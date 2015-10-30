package net.javapla.jawn.core;

import java.text.MessageFormat;

import net.javapla.jawn.core.Response.NoHttpBody;
import net.javapla.jawn.core.exceptions.BadRequestException;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.core.templates.TemplateEngineOrchestrator;

import com.google.inject.Inject;

/**
 * Handling the ControllerResponse using a TemplateManager
 * @author MTD
 */
//perhaps to be called ResponseHandler
public class ResponseRunner {

    private final TemplateEngineOrchestrator templateEngineManager;
    
    @Inject
    public ResponseRunner(TemplateEngineOrchestrator templateEngineManager) {
        this.templateEngineManager = templateEngineManager;
    }
    
    public final void run(final Context context, final Response response) throws ViewException, BadRequestException, MediaTypeException {
        //might already have been handled by the controller or filters
        if (response == null) return;
        
        final Object renderable = response.renderable();
        if (renderable instanceof NoHttpBody) {
            // This indicates that we do not want to render anything in the body.
            // Can be used e.g. for a 204 No Content response or Redirect
            // and bypasses the rendering engines.
            context.finalizeResponse(response, false);
        } else {
            renderWithTemplateEngine(context, response);
        }
    }
    
    private final void renderWithTemplateEngine(final Context context, final Response response) throws ViewException, BadRequestException, MediaTypeException {
        
        // if the response does not contain a content type, we try to look at the request 'accept' header
        if (response.contentType() == null) {
            if (response.supportsContentType(context.getAcceptContentType())) {
                response.contentType(context.getAcceptContentType());
            } else {
                throw new BadRequestException();
            }
        }
        
        final TemplateEngine engine = templateEngineManager.getTemplateEngineForContentType(response.contentType());
        
        if (engine != null) {
            engine.invoke(context, response);
        } else {
            throw new MediaTypeException(
                    MessageFormat.format("Could not find a template engine supporting the content type of the response : {}", response.contentType()));
            //TODO Generic exception holding http method status error
        }
    }
}
