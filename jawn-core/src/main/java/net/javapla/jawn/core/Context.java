package net.javapla.jawn.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.server.WebSocket;
import net.javapla.jawn.core.util.MultiList;

public interface Context /*extends Injection*/ {
    
    interface Response {
        
        Status status();
        
        Response status(int statusCode);
        
        Response header(String name, String value);
        
        Optional<String> header(String name);
        
        Response removeHeader(String name);

        Optional<MediaType> contentType();
        
        Response contentType(MediaType type);
        
        default Response contentType(String contentType) {
            return contentType(MediaType.valueOf(contentType));
        }
        
        Response charset(Charset encoding);
        
        /*Optional<*/Charset/*>*/ charset();
        
        default Response charset(String encoding) {
            try {
                return charset(Charset.forName(encoding));
            } catch (Throwable e) {}
            return this;
        }
        
        Response clearCookie(String name);
        
        Response cookie(Cookie cookie);
        
        default Response cookie(String name, String value) {
            return cookie(Cookie.builder(name, value).build());
        }

        void send(byte[] bytes) throws Exception;
        
        void send(InputStream stream) throws Exception;

        void send(ByteBuffer buf) throws Exception;

        void send(CharBuffer buf) throws Exception;

        void send(CharSequence seq) throws Exception;
        
        OutputStream outputStream();
        
        Writer writer();

        boolean committed();

        
        /*interface Builder {
            Response build(ServerResponse resp);
        }
        
        Context.Response.Builder RESP = (resp) -> {
            return new Context.Response() {
                private final HashMap<String, Cookie> cookies = new HashMap<>();
                
                
                
            };
        };*/
    }
    
    interface Request {

        HttpMethod httpMethod();

        Optional<String> queryString();

        /**
         * IP address of the requester
         * 
         * @return The IP of the caller - does not try to translate proxies
         */
        String ip();
        
        /**
         * IP address of the requesting client.
         * If the IP of the request seems to come from a local proxy,
         * then the "X-Forwarded-For" header is returned.
         *
         * @return IP address of the requesting client.
         */
        default String remoteIp() {
            String remoteAddr = ip();
            
            // This could be a list of proxy IPs, which the developer could
            // provide via some configuration
            if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr)) 
                remoteAddr = header("X-Forwarded-For").value("localhost");
            return remoteAddr;
        }
        
        String remoteAddress();

        /**
         * Get the request URI scheme.  Normally this is one of {@code http} or {@code https}.
         *
         * @return the request URI scheme
         */
        String scheme();
        
        default boolean isSecure() {
            String scheme = scheme();
            return scheme != null && scheme.equalsIgnoreCase("https");
        }
        
        String path();
        
        String context();
        
        Charset charset();
        
        Value header(String name);
        
        MultiList<String> headers();
        
        List<String> headers(String name);
        
        /**
         * 'Accept' header
         * @param contentType
         * @return
         */
        default boolean accept(MediaType contentType) {
            Value header = header("Accept");
            return header.isMissing() || contentType.matches(header.value());
        }
        
        Map<String, Cookie> cookies();

        MediaType contentType();

        Value queryParam(String name);

        MultiList<String> queryParams();

        Value queryParams(String name);
        
        Value pathParam(String name);

        Map<String, String> pathParams();

        MultiList<FormItem> formData();
        
        /**
         * HTTP body. Please don't use this method for form submits. This method is used for getting raw data or a data like json, xml, etc...
         * @return The HTTP body
         * @throws Exception If body can't be converted or there is no HTTP body.
         */
        <T> T body(Class<T> type) throws Exception;
        String body();// throws Exception;
        
        InputStream in() throws IOException;
        byte[] bytes() throws IOException;
        
        /**
         * @return The length, in bytes, of the request body and made available by the input stream, or -1 if the length is not known
         */
        long length();

        default String fullPath() {
            return queryString().map(q -> path() + '?' + q).orElse(path());
        }

        /**
         * Websockets
         */
        void upgrade(WebSocket.Initialiser initialiser);
    }
    
    Request req();
    Response resp();
    Value param(String name);
    
    int serverPort();
    String serverHost();
    
    /**
     * Return the host and port that this request was sent to, in general this will be the value of the <code>Host</code>.
     * If you run behind a reverse proxy, make sure that it has been configured to send the X-Forwarded-Host header.
     * 
     * @return Return the host and port that this request was sent to, in general this will be the value of the <code>Host</code>.
     */
    default String serverHostAndPort() {
        return req()
            .header("X-Forwarded-Host")
            .orElse(req().header("Host"))
            .value(serverHost() + ":" + serverPort());
    }
    
    void attribute(String name, Object value);
    Optional<Object> attribute(String name);
    <T> Optional<T> attribute(String name, Class<T> type);
    void removeAttribute(String name);
    
    Session session();
    Optional<Session> sessionOptionally();
    default Value session(String name) {
        return sessionOptionally().map(sesh -> sesh.get(name)).orElse(Value.empty());
    }
    
    
    /**
     * Returns a String containing the real path for a given virtual path. For example, the path "/index.html" returns
     * the absolute file path on the server's filesystem would be served by a request for
     * "http://host/contextPath/index.html", where contextPath is the context path of this ServletContext.
     * 
     * <p>The real path returned will be in a form appropriate to the computer and operating system on which the servlet
     * container is running, including the proper path separators. This method returns null if the servlet container
     * cannot translate the virtual path to a real path for any reason (such as when the content is being made
     * available from a .war archive).</p>
     *
     * <p>
     * JavaDoc copied from: <a href="http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29">
     * http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29</a>
     * </p>
     *
     * @param file a String specifying a virtual path
     * @return a Path specifying the real path, or null if the translation cannot be performed
     */
    Path realPath(String file);
    
}
