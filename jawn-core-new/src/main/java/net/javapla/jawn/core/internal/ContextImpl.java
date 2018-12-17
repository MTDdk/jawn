package net.javapla.jawn.core.internal;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;
import net.javapla.jawn.core.util.MultiList;

final class ContextImpl implements Context {
    
    private final Request req;
    private final Response resp;
    private final ServerResponse sresp;
    private Route route;
    
    private final HashMap<String, Cookie> cookies = new HashMap<>();
    
    ContextImpl(final ServerRequest req, final ServerResponse resp, final Charset charset) {
        this.sresp = resp;
        
        this.req = new Context.Request() {
            final MediaType contentType = req.header("Content-Type").map(MediaType::valueOf).orElse(MediaType.WILDCARD);
            final Charset cs = contentType.params().get(MediaType.CHARSET_PARAMETER) == null ? charset : Charset.forName(contentType.params().get(MediaType.CHARSET_PARAMETER));
            
            @Override
            public HttpMethod httpMethod() {
                return req.method();
            }
            
            @Override
            public Optional<String> queryString() {
                String s = req.queryString();
                return s.length() == 0 ? Optional.empty() : Optional.of(s);
            }
            
            @Override
            public String ip() {
                return req.ip();
            }
            
            @Override
            public String path() {
                return req.path();
            }
            
            @Override
            public Map<String, Cookie> cookies() {
                return req.cookies().stream().collect(Collectors.toMap(Cookie::name, cookie -> cookie));
            }
            
            @Override
            public MediaType contentType() {
                return contentType;
            }
            
            @Override
            public Charset charset() {
                return cs;
            }
            
            @Override
            public Optional<String> queryParam(final String name) {
                return req.queryParam(name);
            }
            
            @Override
            public MultiList<String> queryParams() {
                return req.queryParams();
            }
            
            @Override
            public List<String> queryParams(final String name) {
                return req.queryParams(name);
            }
            
            @Override
            public MultiList<FormItem> formData() {
                return req.formData();
            }
        };
        
        this.resp = new Context.Response() {
            
            @Override
            public Status status() {
                return Status.valueOf(resp.statusCode());
            }
            
            @Override
            public Response header(final String name, final String value) {
                resp.header(name, value);
                return this;
            }
            
            @Override
            public Response clearCookie(final String name) {
                return cookie(new Cookie.Builder(name, "").maxAge(0).build());
            }
            
            @Override
            public Response cookie(final Cookie cookie) {
                String name = cookie.name();
                // clear cookie?
                if (cookie.maxAge() == 0) {
                    // clear previously set cookie
                    if (cookies.remove(name) == null) {
                        // we add the cookie to send it with an expire header
                        cookies.put(name, cookie);
                    }
                } else {
                    cookies.put(name, cookie);
                }
                return this;
            }
            
            @Override
            public void send(final byte[] bytes) throws Exception {
                resp.send(bytes);
            }
            
            @Override
            public void send(final InputStream stream) throws Exception {
                // we could at this point take a look at the following
                // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range
                // req.header("Range")
                // .. do something where we only read 'end'-'start' amount of bytes of 'stream'
                resp.send(stream);
            }
            
            @Override
            public boolean committed() {
                return resp.committed();
            }
            
        };
    }
    
    @Override
    public Request req() {
        return req;
    }
    
    @Override
    public Response resp() {
        return resp;
    }
    
    @Override
    public Optional<String> param(final String name) {
        return 
            req.queryParam(name)
                .or(() -> Optional
                    .ofNullable(req.formData().first(name)).map(FormItem::value)
                    .orElse(route().map(route -> route.getPathParametersEncoded(req.path()).get(name)))
                )
            ;
    }
    
    void route(final Route route) {
        this.route = route;
    }
    Optional<Route> route() {
        return Optional.ofNullable(route);
    }
    
    void readyResponse(final Result result) {
        if (sresp.committed()) return;
        
        result.contentType()
            .map(MediaType::name).ifPresent(sresp::contentType);
        
        sresp.statusCode(result.status()
            .map(Status::value).orElse(Status.OK.value()));
        
        result.headers()
            .forEach(sresp::header);
        
        writeCookies();
    }
    
    void end() {
        if (!sresp.committed()) {
            writeCookies();
        }
        
        // something, something, content-length header .. ?
        /*sresp.header("Content-Length").or(() -> sresp.header("Transfer-Encoding")).ifPresent(header -> {
            
        });*/
        
        sresp.end();
    }
    
    private void writeCookies() {
        if (!cookies.isEmpty()) {
            List<String> setCookie = cookies.values().stream().map(Cookie::toString).collect(Collectors.toList());
            sresp.header("Set-Cookie", setCookie);
            cookies.clear();
        }
    }
}
