package net.javapla.jawn.server.undertow;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.Req;
import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.URLCodec;

public class UndertowRequest implements Req {
    
    private final HttpServerExchange exchange;
    private final String path;

    public UndertowRequest(final HttpServerExchange exchange) {
        this.exchange = exchange;
        this.path = URLCodec.decode(exchange.getRequestPath(), StandardCharsets.UTF_8);
    }

    @Override
    public String method() {
        return exchange.getRequestMethod().toString();
    }

    @Override
    public String path() {
        return path;
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
    public List<String> params(String name) throws Exception {
        
        Deque<String> deque = exchange.getQueryParameters().get(name);
        return deque;
    }
    
    @Override
    public Optional<String> param(String name) {
        return Optional.ofNullable(exchange.getQueryParameters().get(name).peekFirst());
    }

    @Override
    public List<String> headers(String name) {
        return null;
    }

    @Override
    public Optional<String> header(String name) {
        return null;
    }

    @Override
    public List<String> headerNames() {
        return null;
    }

    @Override
    public List<Cookie> cookies() {
        return null;
    }

    @Override
    public InputStream in() throws Exception {
        return null;
    }

    @Override
    public String ip() {
        return null;
    }

    @Override
    public String protocol() {
        return null;
    }

    @Override
    public boolean secure() {
        return false;
    }

    @Override
    public void startAsync() {
    }

}
