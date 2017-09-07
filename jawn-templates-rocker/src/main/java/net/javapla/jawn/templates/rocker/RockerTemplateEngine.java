package net.javapla.jawn.templates.rocker;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.fizzed.rocker.Rocker;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.templates.TemplateEngine;

public class RockerTemplateEngine implements TemplateEngine {
    
    private static final String TEMPLATE_ENDING = ".rocker.html";
    

    @Override
    public void invoke(Context context, Result response, ResponseStream stream) throws ViewException {
        Map<String, Object> viewObjects = response.getViewObjects();
        
        Rocker.template("");
    }

    @Override
    public String[] getContentType() {
        return new String[]{MediaType.TEXT_HTML};
    }

}
