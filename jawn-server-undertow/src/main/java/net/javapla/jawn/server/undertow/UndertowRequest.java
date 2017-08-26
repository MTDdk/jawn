package net.javapla.jawn.server.undertow;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import io.undertow.server.BlockingHttpExchange;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.FormItem;
import net.javapla.jawn.core.http.Cookie.Builder;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.URLCodec;

public class UndertowRequest implements Request {
    
    // TODO should be instantiated in the core module instead of a server module
    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir")+"/jawn-test");
    static {
        if (!TMP_DIR.exists()) TMP_DIR.mkdir();
    }
    
    private final HttpServerExchange exchange;
    private final String path;
    
    private final FormData form;
    private Supplier<BlockingHttpExchange> blocking;

    public UndertowRequest(final HttpServerExchange exchange) throws IOException {
        this.exchange = exchange;
        
        this.blocking = new MemoizingSupplier<>(() -> this.exchange.startBlocking());
        
        this.form = parseForm(exchange, StandardCharsets.UTF_8.name());//conf.getString("application.tmpdir"), conf.getString("application.charset"));
        
        this.path = URLCodec.decode(exchange.getRequestPath(), StandardCharsets.UTF_8);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.getMethod(exchange.getRequestMethod().toString(), params());
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
    public MultiList<String> params() {
        MultiList<String> params = new MultiList<>();
        
        // query params
        Map<String, Deque<String>> query = exchange.getQueryParameters();
        if (query != null) {
          query.entrySet().stream().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
        }
        // form params
        form.forEach(element -> {
            form.get(element).stream()
                .filter(value -> !value.isFile())
                .forEach(
                    value -> params.put(element, value.getValue()
                )
            );
        });
        
        return params;
    }

    @Override
    public List<String> params(String name) /*throws Exception*/ {
        ArrayList<String> list = new ArrayList<>();
        // query params
        Deque<String> query = exchange.getQueryParameters().get(name);
        if (query != null) {
          query.forEach(list::add);
        }
        
        // form params
        Optional.ofNullable(form.get(name)).ifPresent(values -> {
          values.forEach(value -> {
            if (!value.isFile()) {
              list.add(value.getValue());
            }
          });
        });
        return list;
    }
    
    @Override
    public Optional<String> param(String name) {
        List<String> params = params(name);
        return params.stream().findFirst(); //TODO can it be revised somehow? Bettered?
    }
    
    @Override
    public List<FormItem> files() {
        ArrayList<FormItem> list = new ArrayList<>();
        form.forEach(fieldName -> {
            form.get(fieldName).stream().forEach(value -> {
//                if (value.isFile()) {
                list.add(new UndertowFormItem(value, fieldName));
//                }
            });
        });
        return list;
    }

    @Override
    public List<String> headers(String name) {
        HeaderValues values = exchange.getRequestHeaders().get(name);
        return values == null ? Collections.emptyList() : values;

    }

    @Override
    public Optional<String> header(String name) {
        return Optional.ofNullable(exchange.getRequestHeaders().getFirst(name));
    }

    @Override
    public List<String> headerNames() {
        return exchange.getRequestHeaders().getHeaderNames()
            .stream()
            .map(HttpString::toString)
            .collect(Collectors.toList());
    }

    @Override
    public List<Cookie> cookies() {
        return exchange.getRequestCookies().values().stream()
            .map(UndertowRequest::cookie)
            .collect(Collectors.toList());
    }

    @Override
    public InputStream in() throws IOException {
        blocking.get();
        return exchange.getInputStream();
    }
    
    @Override
    public byte[] bytes() throws IOException {
        try (InputStream stream = in()) {
            ByteArrayOutputStream array = new ByteArrayOutputStream(stream.available());
            return array.toByteArray();
        }
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
    public int port() {
        return exchange.getHostPort();
    }

    /*@Override
    public boolean secure() {
        return exchange.getRequestScheme().equalsIgnoreCase("https");
    }*/
    @Override
    public String scheme() {
        return exchange.getRequestScheme();
    }

    @Override
    public void startAsync() {
        exchange.dispatch();
    }

    
    private FormData parseForm(final HttpServerExchange exchange, final String charset) throws IOException {
        String value = exchange.getRequestHeaders().getFirst("Content-Type");
        if (value != null) {
            if (value.startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                blocking.get();
                return new FormEncodedDataDefinition()
                        .setDefaultEncoding(charset)
                        .create(exchange)
                        .parseBlocking();
            } else if (value.startsWith(MediaType.MULTIPART_FORM_DATA)) {
                blocking.get();
                return new MultiPartParserDefinition()
                        .setTempFileLocation(TMP_DIR.toPath())
                        .setDefaultEncoding(charset)
                        .create(exchange)
                        .parseBlocking();

            }
        }
        return new FormData(0);
    }

    //TODO we really want to extract this to an assigned purpose class
    /**
     * Stolen from Googles Guava Suppliers.java
     * @param delegate
     * @return
     */
    public static <T> Supplier<T> memoizeLock(Supplier<T> delegate) {
        AtomicReference<T> value = new AtomicReference<>();
        return () -> {
            // A 2-field variant of Double Checked Locking.
            T val = value.get();
            if (val == null) {
                synchronized(value) {
                    val = value.get();
                    if (val == null) {
                        val = Objects.requireNonNull(delegate.get());
                        value.set(val);
                    }
                }
            }
            return val;
        };
    }
    
    /**
     * Stolen from Googles Guava Suppliers.java
     * @param delegate
     * @return
     */
    static class MemoizingSupplier<T> implements Supplier<T> {
        final Supplier<T> delegate;
        transient volatile boolean initialized;
        // "value" does not need to be volatile; visibility piggy-backs
        // on volatile read of "initialized".
        transient T value;
        
        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public T get() {
            // A 2-field variant of Double Checked Locking.
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            return "Suppliers.memoize(" + delegate + ")";
        }
        
    }
    
    private static Cookie cookie(final io.undertow.server.handlers.Cookie cookie) {
        Builder bob = Cookie.builder(cookie.getName(), cookie.getValue());
        Optional.ofNullable(cookie.getComment()).ifPresent(bob::setComment);
        Optional.ofNullable(cookie.getDomain()).ifPresent(bob::setDomain);
        Optional.ofNullable(cookie.getPath()).ifPresent(bob::setPath);
        Optional.ofNullable(cookie.getVersion()).ifPresent(bob::setVersion);
        Optional.ofNullable(cookie.getMaxAge()).ifPresent(bob::setMaxAge);
        Optional.ofNullable(cookie.getExpires()).ifPresent(bob::setExpires);
        bob.setHttpOnly(cookie.isHttpOnly());
        bob.setSecure(cookie.isSecure());
        //TODO more?
        return bob.build();
    }

}
