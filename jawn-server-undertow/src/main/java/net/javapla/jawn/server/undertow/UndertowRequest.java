package net.javapla.jawn.server.undertow;

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
import java.util.Map.Entry;
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
import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.Cookie.Builder;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Req;
import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.URLCodec;

public class UndertowRequest implements Req {
    
    private final HttpServerExchange exchange;
    private final String path;
    
    private final FormData form;
    private Supplier<BlockingHttpExchange> blocking;

    public UndertowRequest(final HttpServerExchange exchange) throws IOException {
        this.exchange = exchange;
        
        this.form = parseForm(exchange, System.getProperty("java.io.tmpdir")+"/jawn-test", "UTF-8");//conf.getString("application.tmpdir"), conf.getString("application.charset"));
        this.blocking = new MemoizingSupplier<>(() -> this.exchange.startBlocking());
        
        this.path = URLCodec.decode(exchange.getRequestPath(), StandardCharsets.UTF_8);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.valueOf(exchange.getRequestMethod().toString());
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
        
        // TODO let's just try this§
        HttpServletRequestImpl requestImpl = new HttpServletRequestImpl(exchange, null);
        Map<String, String[]> parameterMap = requestImpl.getParameterMap();
        for (Entry<String, String[]> entry : parameterMap.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        
        
        /*Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        for (Entry<String, Deque<String>> entry : queryParameters.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }*/

        
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
        return Optional.ofNullable(params(name).get(0)); // TODO redo
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
    public InputStream in() throws Exception {
        blocking.get();
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
    public boolean secure() {
        return exchange.getRequestScheme().equalsIgnoreCase("https");
    }

    @Override
    public void startAsync() {
        exchange.dispatch();
    }

    
    private FormData parseForm(final HttpServerExchange exchange, final String tmpdir, final String charset) throws IOException {
        String value = exchange.getRequestHeaders().getFirst("Content-Type");
        if (value != null) {
            MediaType type = MediaType.valueOf(value);
            if (MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(type)) {
                blocking.get();
                return new FormEncodedDataDefinition()
                        .setDefaultEncoding(charset)
                        .create(exchange)
                        .parseBlocking();
            } else if (MediaType.MULTIPART_FORM_DATA_TYPE.equals(type)) {
                blocking.get();
                return new MultiPartParserDefinition()
                        .setTempFileLocation(new File(tmpdir).toPath())
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
        bob.setHttpOnly(cookie.isHttpOnly());
        bob.setSecure(cookie.isSecure());
        //TODO more?
        return bob.build();
    }

}
