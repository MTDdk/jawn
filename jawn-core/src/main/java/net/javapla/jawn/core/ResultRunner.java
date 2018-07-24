package net.javapla.jawn.core;

import java.text.MessageFormat;

import com.google.inject.Inject;

import net.javapla.jawn.core.Result.NoHttpBody;
import net.javapla.jawn.core.exceptions.BadRequestException;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.core.templates.TemplateEngineOrchestrator;

/**
 * Handling the ControllerResponse using a TemplateManager
 * @author MTD
 */
//perhaps to be called ResponseHandler
public final class ResultRunner {

    private final TemplateEngineOrchestrator templateEngineManager;
    
    @Inject
    public ResultRunner(TemplateEngineOrchestrator templateEngineManager) {
        this.templateEngineManager = templateEngineManager;
    }
    
    public final ResponseStream run(final Context context, final Result response) throws ViewException, BadRequestException, MediaTypeException {
        //might already have been handled by the controller or filters
        //if (response == null) return;
        
        final Object renderable = response.renderable();
        if (renderable instanceof NoHttpBody) {
            // This indicates that we do not want to render anything in the body.
            // Can be used e.g. for a 204 No Content response or Redirect
            // and bypasses the rendering engines.
            context.getFlash().clearCurrentFlashCookieData();
            return context.readyResponse(response/*, false*/);//.end();
        } else {
            return renderWithTemplateEngine(context, response);
        }
    }
    
    private final ResponseStream renderWithTemplateEngine(final Context context, final Result response) throws ViewException, BadRequestException, MediaTypeException {
        
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
            ResponseStream rsp = context.readyResponse(response, true);
            engine.invoke(context, response, rsp);
            return rsp;
        } else {
            throw new MediaTypeException(
                    MessageFormat.format("Could not find a template engine supporting the content type of the response : {0}", response.contentType()));
            //TODO Generic exception holding http method status error
        }
    }
}
