package net.javapla.jawn.core.internal.server.undertow;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.URLCodec;

public final class UndertowRequest implements ServerRequest {
    
    // TODO should be instantiated in the core module instead of a server module
    private static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir")+"/jawn" /*+application name*/);
    static {
        if (!TMP_DIR.toFile().exists()) TMP_DIR.toFile().mkdirs();
    }
    

    private final HttpServerExchange exchange;
    private final String path;
    private final Runnable blocking;
    private final HttpMethod method;
    
    private FormData form;
    private MultiList<String> params;
    private MultiList<String> headers;

    public UndertowRequest(final HttpServerExchange exchange, final Config config) {
        this.exchange = exchange;
        this.path = URLCodec.decode(exchange.getRequestPath(), StandardCharsets.UTF_8);
        
        this.blocking = () -> {if(!this.exchange.isBlocking()) this.exchange.startBlocking();};
        
        this.method = HttpMethod.getMethod(exchange.getRequestMethod().toString(), () -> queryParams());
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
        return exchange.getQueryString();
    }

    @Override
    public MultiList<String> queryParams() {
        if (params == null) {
            MultiList<String> params = new MultiList<>();
            
            // query params
            Map<String, Deque<String>> query = exchange.getQueryParameters();
            if (query != null) {
              query.entrySet().stream().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
            }
            // form params
            /*parseForm();
            form.forEach(element -> {
                form.get(element).stream()
                    .filter(value -> !value.isFileItem())
                    .forEach(
                        value -> params.put(element, value.getValue()
                    )
                );
            });*/
            
            this.params = params;
        }
        
        return params;
    }

    @Override
    public MultiList<String> headers() {
        if (headers == null) {
            MultiList<String> h = new MultiList<>();
            
            HeaderMap values = exchange.getRequestHeaders();
            for (var it = values.iterator(); it.hasNext();) {
                HeaderValues header = it.next();
                header.forEach(v -> h.put(header.getHeaderName().toString(), v));
            }
            
            this.headers = h;
        }
        
        return headers;
    }

    @Override
    public List<String> headers(String name) {
        List<String> list = headers().list(name);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public Optional<String> header(String name) {
        return Optional.ofNullable(headers().first(name));
    }

    @Override
    public List<Cookie> cookies() {
        return exchange.getRequestCookies().values().stream().map(UndertowRequest::cookie).collect(Collectors.toList());
    }

    @Override
    public MultiList<FormItem> formData() {
        MultiList<FormItem> list = new MultiList<>();
        
        FormData form = parseForm();
        form.forEach(name -> 
            form.get(name).stream()
                .forEach(value -> list.put(name, new UndertowFormItem(value, name)))
        );
        
        return list;
    }

    @Override
    public InputStream in() throws IOException {
        blocking.run();
        return exchange.getInputStream();
    }

    @Override
    public String ip() {
        return Optional.ofNullable(exchange.getSourceAddress())
            .map(src -> Optional.ofNullable(src.getAddress())
                .map(InetAddress::getHostAddress)
                .orElse(""))
            .orElse("");
    }

    @Override
    public String protocol() {
        return exchange.getProtocol().toString();
    }

    @Override
    public void startAsync(Executor executor, Runnable runnable) {
        exchange.dispatch(executor, runnable);
    }
    
    private FormData parseForm() {
        if (form == null) {
            form = new FormData(0);
            try {
                String charset = StandardCharsets.UTF_8.name();
                String value = exchange.getRequestHeaders().getFirst("Content-Type");
                if (value != null) {
                    if (value.startsWith(MediaType.FORM.name())) {
                        blocking.run();
                        form = new FormEncodedDataDefinition()
                                .setDefaultEncoding(charset)
                                .create(exchange)
                                .parseBlocking();
                    } else if (value.startsWith(MediaType.MULTIPART.name())) {
                        blocking.run();
                        form = new MultiPartParserDefinition()
                                .setTempFileLocation(TMP_DIR)
                                .setDefaultEncoding(charset)
                                .create(exchange)
                                .parseBlocking();
                    }
                }
            } catch (IOException ignore) {}
        }
        return form;
    }

    private static Cookie cookie(final io.undertow.server.handlers.Cookie cookie) {
        Cookie.Builder bob = new Cookie.Builder(cookie.getName(), cookie.getValue());
        Optional.ofNullable(cookie.getComment()).ifPresent(bob::comment);
        Optional.ofNullable(cookie.getDomain()).ifPresent(bob::domain);
        Optional.ofNullable(cookie.getPath()).ifPresent(bob::path);
        Optional.ofNullable(cookie.getVersion()).ifPresent(bob::version);
        Optional.ofNullable(cookie.getMaxAge()).ifPresent(bob::maxAge);
        //Optional.ofNullable(cookie.getExpires()).ifPresent(bob::expires);
        bob.httpOnly(cookie.isHttpOnly());
        bob.secure(cookie.isSecure());
        return bob.build();
    }
}
