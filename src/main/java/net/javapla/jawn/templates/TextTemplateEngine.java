package net.javapla.jawn.templates;

import java.io.IOException;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.Context;
import net.javapla.jawn.ControllerResponse;
import net.javapla.jawn.ResponseStream;

public class TextTemplateEngine implements TemplateEngine {

    @Override
    public void invoke(Context context, ControllerResponse response) {
        ResponseStream stream = context.finalize(response);
        invoke(context, response, stream);
    }
    
    @Override
    public void invoke(Context context, ControllerResponse response, ResponseStream stream) {
        if (response.renderable() == null) return;
        
        try (Writer output = stream.getWriter()) {
            
            output.write(response.renderable().toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        // intentionally null
        return null;
    }

    @Override
    public String[] getContentType() {
        return new String[]{MediaType.TEXT_PLAIN};
    }

}
