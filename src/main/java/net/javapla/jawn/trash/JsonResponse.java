package net.javapla.jawn.trash;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.Context;
import net.javapla.jawn.exceptions.ControllerException;

/**
 * 
 * @author MTD
 */
public class JsonResponse extends ControllerResponse {
    
    private final Object obj;
//    private String locale;
    
    
    protected JsonResponse(Context context, Object obj) {
        super(context);
        this.setContentType(MediaType.APPLICATION_JSON);
        this.obj = obj;
    }
    
    /*protected JsonResponse(Object obj, String locale) {
        this(obj);
        this.locale = locale;
    }*/

    @Override
    void doProcess() {
        if (obj == null) return;
        
        try {
//                JsonParserEngine.writeObject(obj, context.responseWriter());
                context.responseWriter().flush();
        } catch (IOException e) {
            throw new ControllerException(e);
        }
    }

}
