package net.javapla.jawn.core;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public interface Context {
    
    interface Response {
        
        Status status();
        
        Response clearCookie(String name);
        
        Response cookie(Cookie cookie);

        void send(byte[] bytes) throws Exception;
        
        void send(InputStream stream) throws Exception;

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

        Map<String, Cookie> cookies();

        Optional<String> queryString();

        String ip();
        
        String path();

        Charset charset();

        MediaType contentType();
    }
    

    Request req();
    Response resp();
}
