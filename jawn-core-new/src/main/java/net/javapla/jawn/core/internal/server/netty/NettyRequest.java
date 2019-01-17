package net.javapla.jawn.core.internal.server.netty;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCounted;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.util.MultiList;

public class NettyRequest implements ServerRequest {
    // TODO should be instantiated in the core module instead of a server module
    private static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir")+"/jawn" /*+application name*/);
    static {
        if (!TMP_DIR.toFile().exists()) TMP_DIR.toFile().mkdirs();
    }
    
    public static final AttributeKey<String> PROTOCOL = AttributeKey
        .newInstance(NettyRequest.class.getName() + ".protol");

    public static final AttributeKey<Boolean> NEED_FLUSH = AttributeKey
        .newInstance(NettyRequest.class.getName() + ".needFlush");

    public static final AttributeKey<Boolean> ASYNC = AttributeKey
        .newInstance(NettyRequest.class.getName() + ".async");

    public static final AttributeKey<Boolean> SECURE = AttributeKey
        .newInstance(NettyRequest.class.getName() + ".secure");

    
    private final ChannelHandlerContext ctx;
    private final HttpRequest req;
    private final String tmpdir;
    private final QueryStringDecoder query;
    private final String path;
    
    private final HttpMethod method;
    
    private MultiList<String> params;
    private MultiList<FormItem> formData;

    NettyRequest(final ChannelHandlerContext ctx, final HttpRequest req, final String tmpdir) {
        this.ctx = ctx;
        this.req = req;
        this.tmpdir = tmpdir;
        this.query = new QueryStringDecoder(req.uri());
        this.path = query.path(); //any decoding needed?
        
        this.method = HttpMethod.getMethod(req.method().asciiName(), () -> queryParams());
        
        Channel channel = ctx.channel();
        channel.attr(ASYNC).set(false);
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String queryString() {
        String uri = req.uri();
        int at = uri.indexOf('?') + 1;
        return at > 0 && at < uri.length() ? uri.substring(at) : "";
    }

    @Override
    public MultiList<String> queryParams() {
        if (params == null) {
            MultiList<String> params = new MultiList<>(query.parameters());
            this.params = params;
        }
        
        return params;
    }

    @Override
    public MultiList<String> headers() {
        return new MultiList<>(req.headers().entries());
    }

    @Override
    public List<String> headers(String name) {
        return headers().list(name);
    }

    @Override
    public Optional<String> header(String name) {
        String value = req.headers().get(name);
        return Optional.ofNullable(value);
    }

    @Override
    public List<Cookie> cookies() {
        // could be cached
        String cookieString = req.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            return ServerCookieDecoder.STRICT.decode(cookieString).stream()
                .map(NettyRequest::cookie)
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public MultiList<FormItem> formData() {
        decodeFormData();
        return formData;
    }

    @Override
    public InputStream in() throws IOException {
        ByteBuf content = ((HttpContent) req).content();
        return new ByteBufInputStream(content);
    }

    @Override
    public String ip() {
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    @Override
    public String protocol() {
        return ctx.pipeline().get("h2") == null
            ? req.protocolVersion().text()
            : "HTTP/2.0";
    }

    @Override
    public void startAsync(Executor executor, Runnable runnable) {
        Channel channel = ctx.channel();
        channel.attr(NEED_FLUSH).set(false);
        channel.attr(ASYNC).set(true);

        ReferenceCounted body = ((ByteBufHolder) req).content();
        body.retain();
        executor.execute(() -> {
            try {
                runnable.run();
            } finally {
                body.release();
            }
        });
    }
    
    private void decodeFormData() {
        if (formData == null) {
            MultiList<FormItem> data = new MultiList<>();
            
            HttpMethod method = method();
            boolean hasBody = method == HttpMethod.POST || method == HttpMethod.PUT;// || method == HttpMethod.PATCH;
            boolean formLike = false;
            if (req.headers().contains("Content-Type")) {
                String contentType = req.headers().get("Content-Type").toLowerCase();
                formLike = (contentType.startsWith(MediaType.MULTIPART.name()) || contentType.startsWith(MediaType.FORM.name()));
            }
            if (hasBody && formLike) {
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(), req);
                try {
                    Function<HttpPostRequestDecoder, Boolean> hasNext = it -> {
                        try {
                            return it.hasNext();
                        } catch (HttpPostRequestDecoder.EndOfDataDecoderException ex) {
                            return false;
                        }
                    };
                    while (hasNext.apply(decoder)) {
                        HttpData field = (HttpData) decoder.next();
                        String name = field.getName();
                        try {
                            if (field.getHttpDataType() == HttpDataType.FileUpload) {
                                // Is File
                                data.put(name, new NettyFormItem(name, (FileUpload) field, TMP_DIR));
                            } else {
                                // Is Value
                                data.put(name, new NettyFormItem(name, field.getString(StandardCharsets.UTF_8)));
                            }
                        } catch (IOException ignore) {}
                    }
                } finally {
                    decoder.destroy();
                }
            }
            
            this.formData = data;
        }
    }
    
    private static Cookie cookie(final io.netty.handler.codec.http.cookie.Cookie cookie) {
        Cookie.Builder bob = new Cookie.Builder(cookie.name(), cookie.value());
        //Optional.ofNullable(cookie.comment()).ifPresent(bob::comment);
        Optional.ofNullable(cookie.domain()).ifPresent(bob::domain);
        Optional.ofNullable(cookie.path()).ifPresent(bob::path);
        //Optional.ofNullable(cookie.version()).ifPresent(bob::version);
        Optional.ofNullable(cookie.maxAge()).map(Long::intValue).ifPresent(bob::maxAge);
        //Optional.ofNullable(cookie.getExpires()).ifPresent(bob::expires);
        bob.httpOnly(cookie.isHttpOnly());
        bob.secure(cookie.isSecure());
        return bob.build();
    }

}
