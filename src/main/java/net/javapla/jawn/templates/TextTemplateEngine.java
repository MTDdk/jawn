package net.javapla.jawn.templates;

import java.io.IOException;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.Context;
import net.javapla.jawn.NewControllerResponse;
import net.javapla.jawn.ResponseStream;

import com.google.inject.Singleton;

@Singleton
public class TextTemplateEngine implements TemplateEngine {

    @Override
    public void invoke(Context context, NewControllerResponse response) {
        ResponseStream stream = context.finalize(response);
        invoke(context, response, stream);
    }
    
    @Override
    public void invoke(Context context, NewControllerResponse response, ResponseStream stream) {
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
    public String getContentType() {
        return MediaType.TEXT_PLAIN;
    }

}
