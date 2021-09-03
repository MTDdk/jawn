package net.javapla.jawn.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Session;
import net.javapla.jawn.core.SessionStore;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;
import net.javapla.jawn.core.server.WebSocket;
import net.javapla.jawn.core.util.MultiList;

final class ContextImpl implements Context {

    private final Request        req;
    private final Response       resp;
    private final ServerRequest  sreq;
    private final ServerResponse sresp;
    private final DeploymentInfo deploymentInfo;
    private final SessionStore   sessionStore;
    private final Injector       injector;
    private Route                route;

    private HashMap<String, Cookie> cookies;
    private HashMap<String, Object> attributes;
    // private LinkedList<File> files;

    ContextImpl(final ServerRequest sreq,
                final ServerResponse resp,
                final Charset charset,
                final DeploymentInfo deploymentInfo,
                final SessionStore sessionStore,
                final Injector injector) {
        this.deploymentInfo = deploymentInfo;
        this.sessionStore = sessionStore;
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
            public String scheme() {
                return sreq.scheme();
            }
            
            @Override
            public String remoteAddress() {
                return sreq.remoteAddress().getHostString();
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
                if (cookies == null) {
                    List<Cookie> list = sreq.cookies();
                    instantiateCookies(list.size());
                    list.stream().forEach(c -> cookies.put(c.name(), c));
                }
                return cookies;
            }

            @Override
            public MediaType contentType() {
                return sreq.header("Content-Type").orElse(sreq.header("content-type")).map(MediaType::valueOf).orElse(MediaType.WILDCARD);
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
            public Map<String, String> pathParams() {
                return _pathParams();
            }

            @Override
            public MultiList<FormItem> formData() {
                return sreq.formData();
            }

            /*@Override
            public Body body() {
                return new Body() {
                    
                    @Override
                    public InputStream stream() {
                        try {
                            return sreq.in();
                        } catch (IOException e) {
                            throw Up.IO.because(e);
                        }
                    }
                    
                    @Override
                    public long size() {
                        return length();
                    }
                    
                    @Override
                    public boolean inMemory() {
                        return false;
                    }
                    
                    @Override
                    public byte[] bytes() {
                        try {
                            return sreq.bytes();
                        } catch (IOException e) {
                            throw Up.IO.because(e);
                        }
                    }
                };
            }*/

            @Override
            public String body() {
                try {
                    return new String(sreq.bytes(), charset());
                } catch (IOException e) {
                    throw Up.IO.because(e);
                }
            }

            @Override
            public <T> T body(final Class<T> type) throws Exception {
                long length = length();

                if (length > 0) {
                    /*if (type == String.class) {
                        return (T) new String(sreq.bytes(), charset());
                    }
                    if (type == InputStream.class) {
                        return (T) sreq.in();
                    }
                    if (type == byte[].class) {
                        return (T) sreq.bytes();
                    }*/

                    ParserEngineManager engineManager = injector.getInstance(ParserEngineManager.class);
                    ParserEngine engine = engineManager.getParserEngineForContentType(contentType());

                    if (engine == null) {
                        return Value.of(new String(sreq.bytes(), charset())).as(type);

                        // sreq.bytes()/in() might be empty
                        // Clearly we got some body data at this point, but
                        // content-type might just be (unknowingly) wrongly set.
                        // Probably should tell the implementor about this..
                    }

                    return engine.invoke(sreq.in(), type);
                }

                return Value.of(body()).as(type);
            }
            
            @Override
            public InputStream in() throws IOException {
                return sreq.in();
            }
            
            @Override
            public byte[] bytes() throws IOException {
                return sreq.bytes();
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
            private Charset   cs = charset;

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
                    resp.header("Content-Type", contentType + (cs != null ? ("; charset=" + cs) : ""));
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
                // .. do something where we only read 'end'-'start' amount of
                // bytes of 'stream'
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
            public Writer writer() {
                // setContentType();
                return writer = new PrintWriter(outputStream(), false, charset());// new
                                                                                  // OutputStreamWriter(outputStream(),
                                                                                  // charset()));
            }

            @Override
            public boolean committed() {
                return resp.committed();
            }
        };
    }

    private Writer writer;

    @Override
    public Request req() {
        return req;
    }

    @Override
    public Response resp() {
        return resp;
    }
    
    @Override
    public int serverPort() {
        return deploymentInfo.serverPort();
    }
    
    @Override
    public String serverHost() {
        String host = deploymentInfo.serverHost();
        return host.equals("0.0.0.0") ? "localhost" : host;
    }

    @Override
    public Value param(final String name) {
        return Value.of(_pathParam(name)).orElse(sreq.queryParam(name)).orElse(_formData(name));
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
        return Optional.ofNullable(attributeOrNull(name));
    }

    @Override
    public <T> Optional<T> attribute(final String name, final Class<T> type) {
        return attribute(name).map(type::cast);
    }

    @Override
    public void removeAttribute(final String name) {
        if (attributes != null) attributes.remove(name);
    }

    private Object attributeOrNull(final String name) {
        if (attributes == null || attributes.isEmpty()) return null;
        return attributes.get(name);
    }

    @Override
    public Session session() {
        Session sesh = sessionOrNull();
        if (sesh == null) {
            sesh = sessionStore.newSession(this);
            attribute(Session.NAME, sesh);
        }
        return sesh;
    }

    @Override
    public Optional<Session> sessionOptionally() {
        return Optional.ofNullable(sessionOrNull());
    }

    private Session sessionOrNull() {
        Session sesh = (Session) attributeOrNull(Session.NAME);
        if (sesh == null) {
            sesh = sessionStore.findSession(this);
            if (sesh != null) {
                attribute(Session.NAME, sesh);
            }
        }
        return sesh;
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

    void route(final Route route) {
        this.route = route;
    }

    Optional<Route> route() {
        return Optional.ofNullable(route);
    }

    Value _pathParam(final String name) {
        return Value.of(route().map(route -> route.getPathParametersEncoded(req.path()).get(name)));
    }
    
    Map<String, String> _pathParams() {
        if (route != null) {
            return route.getPathParametersEncoded(req.path());
        }
        return Collections.emptyMap();
    }

    Value _formData(final String name) {
        return sreq.formData(name) // TODO might also merit an annotation in mvc
            .map(FormItem::value).map(Value::of).orElse(Value.empty());
    }

    void end() {
        if (!sresp.committed()) {
            writeCookies();
        }

        // something, something, content-length header .. ?
        /*sresp.header("Content-Length").or(() -> sresp.header("Transfer-Encoding")).ifPresent(header -> {
            
        });*/

        if (writer != null) try (Writer w = writer) {
            writer.flush();
        } catch (IOException e) {
            throw Up.IO.because(e);
        }
        sresp.end();
    }

    void done() {
        /*if (files != null) {
            files.forEach(File::delete);
        }*/
        // end();
    }

    /*private*/ void writeCookies() {
        if (cookies != null && !cookies.isEmpty()) {
            List<String> setCookie = cookies.values().stream().map(Cookie::toString).collect(Collectors.toList());
            sresp.header("Set-Cookie", setCookie);
            cookies.clear();
        }
    }
    
    void readyResponse(final Result result) {
        if (!resp.committed()) {
            resp.contentType(result.contentType());
            
            resp.status(result.status().value());
            
            result.charset()
                .ifPresent(resp::charset);
            
            result.headers().
                ifPresent(map -> map.forEach(resp::header));
            
            writeCookies();
        }
    }
    
    /*Object render(final Result result) {
        readyResponse(result);
        if (HttpMethod.HEAD == req.httpMethod()) {
            //context.end();
            return null;
        }
        
        return result.renderable();
    }*/

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
