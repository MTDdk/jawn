package net.javapla.jawn.core.internal.server.netty;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCounted;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.util.MultiList;

public class NettyRequest implements ServerRequest {
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
    private final HttpHeaders responseHeaders;
    private final String tmpdir;
    private final QueryStringDecoder query;
    private final String path;
    private final int wsMaxMessageSize;
    
    private MultiList<String> params;

    NettyRequest(final ChannelHandlerContext ctx, final HttpRequest req, final HttpHeaders responseHeaders, final String tmpdir, final int wsMaxMessageSize) {
        this.ctx = ctx;
        this.req = req;
        this.responseHeaders = responseHeaders;
        this.tmpdir = tmpdir;
        this.query = new QueryStringDecoder(req.uri());
        this.path = query.path(); //any decoding needed?
        this.wsMaxMessageSize = wsMaxMessageSize;
        
        Channel channel = ctx.channel();
        channel.attr(ASYNC).set(false);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.valueOf(req.method().name());
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
        throw new UnsupportedOperationException("Not implented, yet");//TODO
    }

    @Override
    public MultiList<FormItem> formData() {
        return new MultiList<>();//throw new UnsupportedOperationException("Not implented, yet");//TODO
    }

    @Override
    public InputStream in() throws IOException {
        throw new UnsupportedOperationException("Not implented, yet");//TODO
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
    public int port() {
        throw new UnsupportedOperationException("Not implented, yet");//TODO
    }

    @Override
    public String scheme() {
        throw new UnsupportedOperationException("Not implented, yet");//TODO
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

}
