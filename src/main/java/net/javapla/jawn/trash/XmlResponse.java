package net.javapla.jawn.trash;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.Context;
import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.parsers.XmlParser;

/**
 * 
 * @author MTD
 */
public class XmlResponse extends ControllerResponse {

    private final Object obj;
    
    protected XmlResponse(Context context, Object obj) {
        super(context);
        this.setContentType(MediaType.APPLICATION_XML);
        this.obj = obj;
    }

    @Override
    void doProcess() {
        if (obj == null) return;
        
        try {
            XmlParser.writeObject(obj, context.responseWriter());
            context.responseWriter().flush();
        } catch (IOException e) {
            throw new ControllerException(e);
        }
    }

}
