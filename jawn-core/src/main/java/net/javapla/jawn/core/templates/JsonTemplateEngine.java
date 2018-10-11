package net.javapla.jawn.core.templates;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

@Singleton
final class JsonTemplateEngine implements TemplateEngine {

    private final ObjectMapper mapper;
    
    @Inject
    public JsonTemplateEngine(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public final void invoke(final Context context, final Result response, final ResponseStream stream) {
        try (final OutputStream output = stream.getOutputStream()) {
            
            Object object = response.renderable();
            
            if (object instanceof byte[]) {
                output.write((byte[])object);
            } else {
                output.write(mapper.writeValueAsBytes(object));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
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
