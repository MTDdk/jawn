package net.javapla.jawn.core.internal;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.internal.parsers.BodyParsable;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;
import net.javapla.jawn.core.util.MultiList;

final class ContextImpl implements Context {
    
    private final Request req;
    private final Response resp;
    private final ServerResponse sresp;
    private final Injector injector;
    private Route route;
    
    private final HashMap<String, Cookie> cookies = new HashMap<>();
    private HashMap<String, Object> attributes;
    private LinkedList<File> files;
    
    ContextImpl(final ServerRequest sreq, final ServerResponse resp, final Charset charset, final DeploymentInfo deploymentInfo, final Injector injector) {
        this.injector = injector;
        this.sresp = resp;
        
        this.req = new Context.Request() {
            final MediaType contentType = sreq.header("Content-Type").map(MediaType::valueOf).orElse(MediaType.WILDCARD);
            final Charset cs = contentType.params().get(MediaType.CHARSET_PARAMETER) == null ? charset : Charset.forName(contentType.params().get(MediaType.CHARSET_PARAMETER));
            
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
            public Optional<String> header(final String name) {
                return sreq.header(name);
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
                return sreq.cookies().stream().collect(Collectors.toMap(Cookie::name, cookie -> cookie));
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
                return sreq.queryParam(name);
            }
            
            @Override
            public MultiList<String> queryParams() {
                return sreq.queryParams();
            }
            
            @Override
            public List<String> queryParams(final String name) {
                return sreq.queryParams(name);
            }
            
            @Override
            public MultiList<FormItem> formData() {
                return sreq.formData();
            }
            
            @Override
            public Value body() throws Exception {
                long length = length();
                if (length > 0) {
                    MediaType type = contentType();
                    Config config = injector.getInstance(Config.class);
                    ParserEngineManager engine = injector.getInstance(ParserEngineManager.class);
                    
                    // TODO TMPdir is also used in server implementations and should be a part of Config instead
                    String tmp = Paths.get(System.getProperty("java.io.tmpdir")+"/jawn" /*+application name*/).toString();
                    File body = new File(tmp, Integer.toHexString(System.identityHashCode(this)));
                    addFile(body);
                    
                    int bufferSize = config.getInt("server.http.request.buffer_size");
                    
                    // body work
                    return new ValueImpl(engine, type, new BodyParsable(length, cs, body, sreq.in(), bufferSize));
                }
                
                return new ValueImpl.Empty();
            }
            
            @Override
            public long length() {
                return sreq.header("Content-Length").map(Long::parseLong).orElse(-1l);
            }
            
        };
        
        this.resp = new Context.Response() {
            private MediaType contentType;
            private Charset cs;
            
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
            public Optional<String> header(final String name) {
                return resp.header(name);
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
    
    /* Context should not be responsible of this
     * @Override
    public <T> T require(final Key<T> key) {
        return injector.getProvider(key).get();//injector.require(key);
    }*/
    
    @Override
    public String realPath(final String file) {
        return injector.getInstance(DeploymentInfo.class).getRealPath(file);
    }
    
    void route(final Route route) {
        this.route = route;
    }
    Optional<Route> route() {
        return Optional.ofNullable(route);
    }
    
    // TODO could probably just as well be handled by ResultRunner
    void readyResponse(final Result result) {
        if (sresp.committed()) return;
        
        result.contentType()
            .map(MediaType::name).ifPresent(resp::contentType);
        
        sresp.statusCode(result.status()
            .map(Status::value).orElse(200));
        
        result.charset()
            .ifPresent(resp::charset);
        
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
    
    void done() {
        if (files != null) {
            files.forEach(File::delete);
        }
    }
    
    private void writeCookies() {
        if (!cookies.isEmpty()) {
            List<String> setCookie = cookies.values().stream().map(Cookie::toString).collect(Collectors.toList());
            sresp.header("Set-Cookie", setCookie);
            cookies.clear();
        }
    }
    
    private void instantiateAttributes() {
        if (attributes == null) attributes = new HashMap<>(5);
    }
    private void addFile(final File file) {
        if (files == null) files = new LinkedList<>();
        files.add(file);
    }
}
