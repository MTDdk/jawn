package net.javapla.jawn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.exceptions.BadRequestException;
import net.javapla.jawn.exceptions.MediaTypeException;
import net.javapla.jawn.exceptions.RouteException;
import net.javapla.jawn.exceptions.ViewException;
import net.javapla.jawn.i18n.Lang;
import net.javapla.jawn.util.StringUtil;

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
    public void runRequest(Context context) {
        
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
        
      //TODO first do this language extraction IF custom route not found
        String language = findLanguagePrefix(uri, lang);
            
        //MTD: we first look for a language prefix and strip the URI if it is found
        if (!StringUtil.blank(language)) {
            uri = uri.substring(language.length() +1 );
//            if (uri.isEmpty()) uri = "/";
        } else {
            language = lang.getDefaultLanguage();
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
    
    
    /**
     * Finds the language segment of the URI if the language property is set.
     * Just extracts the first segment.
     * @param uri The full URI
     * @return the extracted language segment. null, if none is found
     * @author MTD
     */
    //TODO perhaps refactor into some LangHelper
    protected String findLanguagePrefix(String uri, Lang language) {
        if ( ! language.areLanguagesSet()) return null;
        String lang = uri.startsWith("/") ? uri.substring(1) : uri;
        lang = lang.split("/")[0];
        
        if(language.isLanguageSupported(lang)) return lang;
        
        //TODO we probably want to throw some exceptions
        return null;
    }
}
