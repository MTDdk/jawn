package net.javapla.jawn.templates;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.Context;
import net.javapla.jawn.NewControllerResponse;
import net.javapla.jawn.ResponseStream;

@Singleton
class JsonTemplateEngine implements TemplateEngine {

    private final ObjectMapper mapper;
    
    @Inject
    public JsonTemplateEngine(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public void invoke(Context context, NewControllerResponse response) {
        ResponseStream stream = context.finalize(response);
        invoke(context, response, stream);
    }
    
    @Override
    public void invoke(Context context, NewControllerResponse response, ResponseStream stream) {
        try (OutputStream output = stream.getOutputStream()) {
            
            mapper.writeValue(output, response.renderable());
            
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
        return MediaType.APPLICATION_JSON;
    }

    
}
