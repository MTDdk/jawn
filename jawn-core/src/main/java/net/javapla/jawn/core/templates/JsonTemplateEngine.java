package net.javapla.jawn.core.templates;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

@Singleton
final class JsonTemplateEngine implements TemplateEngine {

    private final ObjectWriter writer;
    
    @Inject
    public JsonTemplateEngine(ObjectMapper mapper) {
        writer = mapper.writer();
    }
    
    @Override
    public final void invoke(final Context context, final Response response, final ResponseStream stream) {
        try (final OutputStream output = stream.getOutputStream()) {
            
            writer.writeValue(output, response.renderable());
            
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
        return new String[]{MediaType.APPLICATION_JSON};
    }
    /*@Override
    public ContentType[] getContentType2() {
        return new ContentType[]{ContentType.APPLICATION_JSON};
    }*/

    
}
