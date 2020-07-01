package net.javapla.jawn.core;

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

        String ip();
        
        String path();
        
        String context();
        
        Charset charset();
        
        Value header(String name);
        
        MultiList<String> headers();
        
        List<String> headers(String name);
        
        Map<String, Cookie> cookies();

        MediaType contentType();

        Value queryParam(String name);

        MultiList<String> queryParams();

        Value queryParams(String name);
        
        Value pathParam(String name);

        MultiList<FormItem> formData();
        
        /**
         * HTTP body. Please don't use this method for form submits. This method is used for getting raw data or a data like json, xml, etc...
         * @return The HTTP body
         * @throws Exception If body can't be converted or there is no HTTP body.
         */
        String body() throws Exception;
        <T> T body(Class<T> type) throws Exception;

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
    
    void attribute(String name, Object value);
    Optional<Object> attribute(String name);
    <T> Optional<T> attribute(String name, Class<T> type);
    void removeAttribute(String name);
    
    
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
