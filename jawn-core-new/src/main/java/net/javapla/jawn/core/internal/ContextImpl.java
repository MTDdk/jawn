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
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;


//TODO, thoughts: 
// Have a context unifying server.Request and server.Response OR create separate Request and Response with logic and convert
// current server.Request -> server.ServerRequest/NativeRequest +  server.Response -> server.ServerResponse/NativeResponse
class ContextImpl implements Context {
    
    private final Request req;
    private final Response resp;
    private final ServerResponse sresp;
    
    private final HashMap<String, Cookie> cookies = new HashMap<>();
    
    public ContextImpl(final ServerRequest req, final ServerResponse resp, final Charset charset) {
        this.sresp = resp;
        
        this.req = new Context.Request() {
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
                // can be made final if we discover it to be called often
                return req.header("Content-Type").map(MediaType::valueOf).orElse(MediaType.WILDCARD);
            }
            
            @Override
            public Charset charset() {
                /*String cs = contentType().params().get(MediaType.CHARSET_PARAMETER);
                return cs != null ? Charset.forName(cs) : charset;*/
                return charset;
            }
        };
        
        this.resp = new Context.Response() {
            
            @Override
            public Status status() {
                return Status.valueOf(resp.statusCode());
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
    
    void readyResponse(final Result result) {
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
        
        // something, something, content-length header ..
        sresp.header("Content-Length").or(() -> sresp.header("Transfer-Encoding")).ifPresent(header -> {
            
        });
        
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
