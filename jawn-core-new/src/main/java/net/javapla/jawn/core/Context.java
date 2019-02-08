package net.javapla.jawn.core;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.util.MultiList;

public interface Context extends Injection {
    
    interface Response {
        
        Status status();
        
        Response header(String name, String value);
        
        Optional<String> header(String name);
        
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

        void send(byte[] bytes) throws Exception;
        
        void send(InputStream stream) throws Exception;

        void send(ByteBuffer buf) throws Exception;

        void send(CharBuffer buf) throws Exception;

        void send(CharSequence seq) throws Exception;

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
        
        Optional<String> header(String name);
        
        MultiList<String> headers();
        
        List<String> headers(String name);
        
        Map<String, Cookie> cookies();

        MediaType contentType();

        Optional<String> queryParam(String name);

        MultiList<String> queryParams();

        List<String> queryParams(String name);

        MultiList<FormItem> formData();

        /**
         * @return The length, in bytes, of the request body and made available by the input stream, or -1 if the length is not known
         */
        long length();
    }
    

    Request req();
    Response resp();
    Optional<String> param(String name);
    void attribute(String name, Object value);
    Optional<Object> attribute(String name);
    <T> Optional<T> attribute(String name, Class<T> type);
}
