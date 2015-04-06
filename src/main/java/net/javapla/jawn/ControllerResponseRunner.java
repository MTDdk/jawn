package net.javapla.jawn;

import net.javapla.jawn.exceptions.BadRequestException;
import net.javapla.jawn.templates.TemplateEngine;
import net.javapla.jawn.templates.TemplateEngineManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
//ResultHandler
public class ControllerResponseRunner {

    private final TemplateEngineManager templateEngineManager;
    
    @Inject
    public ControllerResponseRunner(TemplateEngineManager templateEngineManager) {
        this.templateEngineManager = templateEngineManager;
    }
    
    public void run(Context context, NewControllerResponse response) {
        //might already have been handled by the controller
        if (response == null) return;
        
        
        
        Object renderable = response.renderable();
        if (renderable instanceof NoHttpBody) { // no http body
            // This indicates that we do not want to render anything in the body.
            // Can be used e.g. for a 204 No Content response.
            // and bypasses the rendering engines.
            context.finalize(response);
        } else {
            renderWithTemplateEngine(context, response);
        }
    }
    
    private void renderWithTemplateEngine(Context context, NewControllerResponse response) throws BadRequestException {
        
        // if the response does not contain a content type, we try to look at the request 'accept' header
        if (response.contentType() == null) {
            if (response.supportsContentType(context.getAcceptContentType())) {
                response.contentType(context.getAcceptContentType());
            } else {
                throw new BadRequestException();
            }
        }
        
        TemplateEngine engine = templateEngineManager.getTemplateEngineForContentType(response.contentType());
        
        if (engine != null) {
            engine.invoke(context, response);
        } else {
            throw new RuntimeException();//TODO Generic exception holding http method status error
        }
    }
}
