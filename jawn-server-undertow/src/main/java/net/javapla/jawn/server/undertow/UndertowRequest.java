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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.Cookie.Builder;
import net.javapla.jawn.core.http.FormItem;
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
    private final String contextPath;
    
    private FormData form;
    private final Runnable blocking;
    

    public UndertowRequest(final HttpServerExchange exchange, final String contextPath) throws IOException {
        this.exchange = exchange;
        
        this.blocking = () -> {if(!this.exchange.isBlocking()) this.exchange.startBlocking();};
        
        //this.form = parseForm(exchange, StandardCharsets.UTF_8.name());//conf.getString("application.tmpdir"), conf.getString("application.charset"));
        
        this.contextPath = contextPath;
        this.path = URLCodec.decode(DeploymentInfo.stripContextPath(contextPath, exchange.getRequestPath()), StandardCharsets.UTF_8);
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
    public String contextPath() {
        return contextPath;
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
        parseForm();
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
        parseForm();
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
        parseForm();
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
    public Map<String, Cookie> cookies() {
        Stream<Entry<String, io.undertow.server.handlers.Cookie>> stream = exchange.getRequestCookies().entrySet().stream();
        return stream.collect(Collectors.toMap(Map.Entry::getKey, UndertowRequest::cookie));
    }

    @Override
    public InputStream in() throws IOException {
        blocking.run();
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
        exchange.dispatch(); //TODO https://github.com/jooby-project/jooby/blob/master/modules/jooby-undertow/src/main/java/org/jooby/internal/undertow/UndertowRequest.java
    }

    private FormData parseForm() {
        if (form == null) {
            form = new FormData(0);
            try {
                String charset = StandardCharsets.UTF_8.name();
                String value = exchange.getRequestHeaders().getFirst("Content-Type");
                if (value != null) {
                    if (value.startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
                        blocking.run();
                        form = new FormEncodedDataDefinition()
                                .setDefaultEncoding(charset)
                                .create(exchange)
                                .parseBlocking();
                    } else if (value.startsWith(MediaType.MULTIPART_FORM_DATA)) {
                        blocking.run();
                        form = new MultiPartParserDefinition()
                                .setTempFileLocation(TMP_DIR.toPath())
                                .setDefaultEncoding(charset)
                                .create(exchange)
                                .parseBlocking();
                    }
                }
            } catch (IOException ignore) {}
        }
        return form;
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
    private static Cookie cookie(Map.Entry<String, io.undertow.server.handlers.Cookie> cookieEntry) {
        return cookie(cookieEntry.getValue());
    }

}
