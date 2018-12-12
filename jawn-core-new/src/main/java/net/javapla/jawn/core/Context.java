package net.javapla.jawn.core;

import java.util.Map;
import java.util.Optional;

public interface Context {
    
    interface Response {
        
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
    }
    

    Request req();
    Response resp();
}
