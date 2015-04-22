package net.javapla.jawn.core;

import net.javapla.jawn.core.exceptions.BadRequestException;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.exceptions.RouteException;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.i18n.Lang;
import net.javapla.jawn.core.i18n.LanguagesNotSetException;
import net.javapla.jawn.core.i18n.NotSupportedLanguageException;
import net.javapla.jawn.core.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

//NinjaDefault
public class FrameworkEngine {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private final Router router;
    private final ResponseRunner runner;
    private final Lang lang;
    private final Injector injector;
    
    @Inject
    public FrameworkEngine(Router router, ResponseRunner runner, Lang lang, Injector injector) {
        this.router = router;
        this.runner = runner;
        this.lang = lang;
        this.injector = injector;
    }
    
    //onRouteRequest
    public void runRequest(Context.Internal context) {
        String path = context.path();
        
        String format = null;
        String uri;
        // look for any format in the request
        if(path.contains(".")){
            uri = path.substring(0, path.lastIndexOf('.'));
            format = path.substring(path.lastIndexOf('.') + 1);
        }else{
            uri = path;
        }
        
        //README maybe first do this language extraction IFF custom route not found
        String language;
        try {
            // if languages are set we try to deduce them
            language = lang.deduceLanguageFromUri(uri);
            if (!StringUtil.blank(language))
                uri = uri.substring(language.length() +1 ); // strip the language prefix from the URI
            else
                language = lang.getDefaultLanguage();
        } catch (LanguagesNotSetException e) {
            language = null;
        } catch (NotSupportedLanguageException e) {
            // use the default language
            language = lang.getDefaultLanguage();
            // We cannot be sure that the URI actually contains
            // a language parameter, so we let the router handle this
        }
            
        if (StringUtil.blank(uri)) {
            uri = "/";//different servlet implementations, damn.
        }
        
        try {
            
            Route route = router.getRoute(context.getHttpMethod(), uri, injector);
            context.setRouteInformation(route, format, language, uri);
            
            if (route != null) {
                // run pre-filters
                ControllerResponse response = route.getFilterChain().before(context);
                
                runner.run(context, response);
                
                // run post-filters
                route.getFilterChain().after(context);
            } else {
                
                // This scenario ought not happen as the Router#getRoute() would have thrown an exception
                // if no route is found
                logger.warn("No matching route for servlet path: " + context.path() + ", passing down to container.");
            }
            //TODO do something about the exceptions!
        } catch (RouteException e) {
            // 404
            e.printStackTrace();
        } catch (ViewException e) {
            // 500
            e.printStackTrace();
        } catch (BadRequestException | MediaTypeException e) {
            // 400
            e.printStackTrace();
        } catch (Exception e) {
            // 500
            e.printStackTrace();
        }
    }
    
    public void onFrameworkStartup() {
        
    }
    
    public void onFrameworkShutdown() {
        
    }
}
