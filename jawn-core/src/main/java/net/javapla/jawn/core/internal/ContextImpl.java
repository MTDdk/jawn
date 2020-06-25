package net.javapla.jawn.core.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.WebSocket;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;
import net.javapla.jawn.core.util.MultiList;

final class ContextImpl implements Context {
    
    private final Request req;
    private final Response resp;
    private final ServerRequest sreq;
    private final ServerResponse sresp;
    private final Injector injector;
    private Route route;
    
    private HashMap<String, Cookie> cookies;
    private HashMap<String, Object> attributes;
    //private LinkedList<File> files;
    
    ContextImpl(final ServerRequest sreq, final ServerResponse resp, final Charset charset, final DeploymentInfo deploymentInfo, final Injector injector) {
        this.injector = injector;
        this.sreq = sreq;
        this.sresp = resp;
        
        this.req = new Context.Request() {
            
            @Override
            public HttpMethod httpMethod() {
                return sreq.method();
            }
            
            @Override
            public Optional<String> queryString() {
                String s = sreq.queryString();
                return s.length() == 0 ? Optional.empty() : Optional.of(s);
            }
            
            @Override
            public String ip() {
                return sreq.ip();
            }
            
            @Override
            public String path() {
                return deploymentInfo.stripContextPath(sreq.path());
            }
            
            @Override
            public String context() {
                return deploymentInfo.getContextPath();
            }
            
            @Override
            public Value header(final String name) {
                return Value.of(sreq.header(name));
            }
            
            @Override
            public MultiList<String> headers() {
                return sreq.headers();
            }
            
            @Override
            public List<String> headers(final String name) {
                return sreq.headers(name);
            }
            
            @Override
            public Map<String, Cookie> cookies() {
                List<Cookie> list = sreq.cookies();
                instantiateCookies(list.size());
                list.stream().forEach(c -> cookies.put(c.name(), c));
                return cookies;
            }
            
            @Override
            public MediaType contentType() {
                return sreq.header("Content-Type").map(MediaType::valueOf).orElse(MediaType.WILDCARD);
            }
            
            @Override
            public Charset charset() {
                String charsetName = contentType().params().get(MediaType.CHARSET_PARAMETER);
                return charsetName == null ? charset : Charset.forName(charsetName);
            }
            
            @Override
            public Value queryParam(final String name) {
                return Value.of(sreq.queryParam(name));
            }
            
            @Override
            public MultiList<String> queryParams() {
                return sreq.queryParams();
            }
            
            @Override
            public Value queryParams(final String name) {
                return Value.of(sreq.queryParams(name));
            }
            
            @Override
            public Value pathParam(final String name) {
                return Value.of(_pathParam(name));
            }
            
            @Override
            public MultiList<FormItem> formData() {
                return sreq.formData();
            }
            
            @Override
            public String body() throws Exception {
                return body(String.class);
            }
            
            @Override
            public <T> T body(final Class<T> type) throws Exception {
                long length = length();
                
                if (length > 0) {
                    ParserEngineManager engineManager = injector.getInstance(ParserEngineManager.class);
                    ParserEngine engine = engineManager.getParserEngineForContentType(contentType());
                    
                    if (engine == null) {
                        return Value.of(new String(sreq.bytes(), charset())).as(type);
                        
                        // sreq.bytes()/in() might be empty
                        // Clearly we got some body data at this point, but content-type might just be (unknowingly) wrongly set.
                        // Probably should tell the implementor about this..
                    }
                    
                    return engine.invoke(sreq.in(), type);
                }
                
                return Value.of(new String(sreq.bytes(), charset())).as(type);
            }
            
            @Override
            public long length() {
                return sreq.header("Content-Length").map(Long::parseLong).orElse(-1l);
            }
            
            @Override
            public void upgrade(WebSocket.Initialiser initialiser) {
                sreq.upgrade(this, initialiser);
            }
        };
        
        this.resp = new Context.Response() {
            private MediaType contentType;
            private Charset cs = charset;
            
            @Override
            public Status status() {
                return Status.valueOf(resp.statusCode());
            }
            
            @Override
            public Response status(int statusCode) {
                resp.statusCode(statusCode);
                return this;
            }
            
            @Override
            public Response header(final String name, final String value) {
                resp.header(name, value);
                return this;
            }
            
            @Override
            public Optional<String> header(final String name) {
                return resp.header(name);
            }
            
            @Override
            public Response removeHeader(final String name) {
                resp.removeHeader(name);
                return this;
            }
            
            @Override
            public Optional<MediaType> contentType() {
                return Optional.ofNullable(contentType);
            }

            @Override
            public Response contentType(final MediaType contentType) {
                this.contentType = contentType;
                setContentType();
                return this;
            }
            
            @Override
            public Response charset(final Charset encoding) {
                cs = encoding;
                setContentType();
                return this;
            }

            @Override
            public Charset charset() {
                return cs != null ? cs : req.charset();
            }
            
            private void setContentType() {
                if (contentType != null) {
                    resp.header("Content-Type", contentType + (cs != null ? ("; charset=" + cs) : "") );
                }
            }
            
            @Override
            public Response clearCookie(final String name) {
                return cookie(new Cookie.Builder(name, "").maxAge(0).build());
            }
            
            @Override
            public Response cookie(final Cookie cookie) {
                instantiateCookies(5);
                
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
            public void send(final ByteBuffer buf) throws Exception {
                resp.send(buf);
            }
            
            @Override
            public void send(final CharBuffer buf) throws Exception {
                resp.send(charset().encode(buf));
            }
            
            @Override
            public void send(final CharSequence seq) throws Exception {
                resp.send(charset().encode(CharBuffer.wrap(seq)));
            }
            
            @Override
            public OutputStream outputStream() {
                return resp.outputStream();
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
    public Value param(final String name) {
        return 
            Value.of(_pathParam(name))
                 .orElse(sreq.queryParam(name))
                 .orElse(_formData(name));
    }
    
    /*public Value params() {
        MultiList<String> queryParams = sreq.queryParams();
        MultiList<Optional<String>> formData = req.formData().map(FormItem::value);
        Stream.concat(queryParams, null)        
        return null;
    }*/
    
    @Override
    public void attribute(final String name, final Object value) {
        instantiateAttributes();
        attributes.put(name, value);
    }
    
    @Override
    public Optional<Object> attribute(final String name) {
        if (attributes == null || attributes.isEmpty()) return Optional.empty();
        return Optional.ofNullable(attributes.get(name));
    }
    
    @Override
    public <T> Optional<T> attribute(final String name, final Class<T> type) {
        return attribute(name).map(type::cast);
    }
    
    @Override
    public void removeAttribute(final String name) {
        if (attributes != null) attributes.remove(name);
    }
    
    /* Context should not be responsible of this
     * @Override
    public <T> T require(final Key<T> key) {
        return injector.getProvider(key).get();//injector.require(key);
    }*/
    
    @Override
    public Path realPath(final String file) { // Do we even need this method?
        return Paths.get(injector.getInstance(DeploymentInfo.class).getRealPath(file));
    }
    
    /*@Override
    public Context upgrade(WebSocket.Initialiser initialiser) {
        sreq.upgrade(initialiser)
        return null;
    }*/
    
    /**
     * IP address of the requesting client.
     * If the IP of the request seems to come from a local proxy,
     * then the X-Forwarded-For header is returned.
     *
     * @return IP address of the requesting client.
     */
    //@Override //TODO
    public String remoteIP(){
        String remoteAddr = req.ip();
        
        // This could be a list of proxy IPs, which the developer could
        // provide via some configuration
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr)) 
            remoteAddr = req.header("X-Forwarded-For").value("localhost");
        return remoteAddr;
    }
    
    void route(final Route route) {
        this.route = route;
    }
    Optional<Route> route() {
        return Optional.ofNullable(route);
    }
    
    Value _pathParam(final String name) {
        return Value.of(
                    route().map(route -> route.getPathParametersEncoded(req.path()).get(name))
                );
    }
    
    Value _formData(final String name) {
        return sreq
                .formData(name) // TODO might also merit an annotation in mvc
                .map(FormItem::value)
                .map(Value::of)
                .orElse(Value.empty());
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
    
    void done() {
        /*if (files != null) {
            files.forEach(File::delete);
        }*/
        end();
    }
    
    /*private*/ void writeCookies() {
        if (cookies != null && !cookies.isEmpty()) {
            List<String> setCookie = cookies.values().stream().map(Cookie::toString).collect(Collectors.toList());
            sresp.header("Set-Cookie", setCookie);
            cookies.clear();
        }
    }
    
    private void instantiateAttributes() {
        if (attributes == null) attributes = new HashMap<>(5);
    }
    private void instantiateCookies(int initial) {
        if (cookies == null) cookies = new LinkedHashMap<>(initial);
    }
    /*private void addFile(final File file) {
        if (files == null) files = new LinkedList<>();
        files.add(file);
    }*/
}
