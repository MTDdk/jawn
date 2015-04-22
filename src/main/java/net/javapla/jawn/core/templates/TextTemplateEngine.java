package net.javapla.jawn.core.templates;

import java.io.IOException;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.ControllerResponse;
import net.javapla.jawn.core.ResponseStream;

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
