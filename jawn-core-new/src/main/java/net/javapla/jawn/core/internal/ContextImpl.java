package net.javapla.jawn.core.internal;

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
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;


//TODO, thoughts: 
// Have a context unifying server.Request and server.Response OR create separate Request and Response with logic and convert
// current server.Request -> server.ServerRequest/NativeRequest +  server.Response -> server.ServerResponse/NativeResponse
class ContextImpl implements Context {
    
    private final Request req;
    private final Response resp;
    private final ServerRequest sreq;
    private final ServerResponse sresp;
    private final Route route;
    
    private final HashMap<String, Cookie> cookies = new HashMap<>();
    
    public ContextImpl(final ServerRequest req, final ServerResponse resp, final Route route) {
        this.sreq = req;
        this.sresp = resp;
        this.route = route;
        
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
        };
        
        this.resp = new Context.Response() {
            
            
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
    
    void execute(final Result result) {
        
        result.contentType()
            .map(MediaType::name).ifPresent(sresp::contentType);
        
        sresp.statusCode(result.status()
            .map(Status::value).orElse(Status.OK.value()));
        
        result.headers()
            .forEach(sresp::header);
        
        writeCookies();
        
        if (HttpMethod.HEAD == req.httpMethod()) {
            // end();
            return;
        }
        
    }
    
    private void writeCookies() {
        if (!cookies.isEmpty()) {
            List<String> setCookie = cookies.values().stream().map(Cookie::toString).collect(Collectors.toList());
            sresp.header("Set-Cookie", setCookie);
            cookies.clear();
        }
    }
}
