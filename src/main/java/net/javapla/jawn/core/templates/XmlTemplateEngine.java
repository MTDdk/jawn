package net.javapla.jawn.core.templates;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class XmlTemplateEngine implements TemplateEngine {

    private final XmlMapper mapper;
    
    @Inject
    public XmlTemplateEngine(XmlMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public void invoke(Context context, Response response) {
        ResponseStream stream = context.finalizeResponse(response);
        invoke(context, response, stream);
    }
    
    
    @Override
    public void invoke(Context context, Response response, ResponseStream stream) {
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
    public String[] getContentType() {
        return new String[]{MediaType.APPLICATION_XML};
    }

    
}
