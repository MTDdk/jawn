package net.javapla.jawn.core.templates;

import java.io.IOException;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

public class TextTemplateEngine implements TemplateEngine {

    @Override
    public void invoke(Context context, Response response) {
        ResponseStream stream = context.finalizeResponse(response);
        invoke(context, response, stream);
    }
    
    @Override
    public void invoke(Context context, Response response, ResponseStream stream) {
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
