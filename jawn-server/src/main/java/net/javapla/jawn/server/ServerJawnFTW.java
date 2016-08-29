package net.javapla.jawn.server;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.javapla.jawn.core.FrameworkEngine;
import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.exceptions.InitException;
import net.javapla.jawn.core.http.Req;
import net.javapla.jawn.core.http.Resp;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.server.api.HttpHandler;
import net.javapla.jawn.server.api.Server;

public class ServerJawnFTW implements HttpHandler {
    
    
protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private String[] exclusions;

    protected final Injector injector;

    protected final ServerBootstrap bootstrapper;
    protected final FrameworkEngine engine;
    
    public ServerJawnFTW() throws Exception {
        bootstrapper = new ServerBootstrap(this);
        bootstrapper.boot();
        injector = bootstrapper.getInjector();
        engine = injector.getInstance(FrameworkEngine.class);
        
        injector.getInstance(Server.class).start();
        
        config();
    }
    
    void config() {
        // find paths inside webapp, that are NOT WEB-INF
        Set<String> exclusionPaths = findExclusionPaths();
        // and convert them to an array for fast lookup
        exclusions = exclusionPaths.toArray(new String[exclusionPaths.size()]);
        logger.debug("Letting the server take care of providing resources from: {}", exclusionPaths);
        
        
        // either the encoding was set by the user, or we default
        //TODO make encoding configurable
//        String enc = appContext.getAsString(AppContext.ENCODING);
//        if (enc == null) {
//            enc = Constants.ENCODING;
//            appContext.set(AppContext.ENCODING, enc);
//        }
//        logger.debug("Setting encoding: " + enc);
        
//        root_controller = filterConfig.getInitParameter("root_controller");
        
        
        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(PropertiesImpl.class).getMode());
    }
    
    /**
     * Actually sorts the paths, which is not appreciated and not even used anywhere
     * @param servletContext2 
     * @return
     */
    private Set<String> findExclusionPaths() {
        Set<String> exclusions = new TreeSet<String>();
        
        // Let other handlers deal with folders that do not reside in the WEB-INF or META-INF
        List<String> collect = Arrays.asList(new File("webapp").list());
        
        // This most certainly should not be null!
        // It means that the server cannot read files at all
        if (collect == null || collect.isEmpty()) throw new InitException("ServletContext cannot read files. Reason is unknown");

        
        Set<String> resourcePaths = new TreeSet<>(collect);//servletContext.getResourcePaths("/");
        resourcePaths.removeIf( path -> path.contains("-INF") || path.contains("-inf"));
    
        // We still need to also remove the views folder from being processed by other handlers
        resourcePaths.removeIf( path -> path.contains(TemplateEngine.TEMPLATES_FOLDER));
        
        // Add the remaining paths to exclusions
        for (String path : resourcePaths) {
            // add leading slash
            if (path.charAt(0) != '/')
                path = '/' + path;
            
            // remove the last slash
            if (path.charAt(path.length()-1) == '/')
                path = path.substring(0, path.length()-1);
            exclusions.add(path);
        }
        
        return exclusions;
    }
    
    protected static final boolean redirectUrlEndingWithSlash(final String path, final HttpServletResponse response) throws IOException {
        if (path.length() > 1 && path.charAt(path.length()-1) == '/') {  //ends with
            response.sendRedirect(path.substring(0, path.length()-1));
            return true;
        }
        return false;
    }
    
    /**
     * Instead of just verifying if a path contains an exclusion, the method returns the exclusion.
     * This takes care of some resources being prepended with the language prefix.
     * Like:
     *      http://localhost/da/images/something.jpg
     * should be the same as
     *      http://localhost/en/images/something.jpg,
     *      http://localhost/images/something.jpg
     * @author MTD
     * 
     * @return The full <code>servletPath</code> if can be seen as a resource from any of the exclusions
     */
    protected final String translateResource(String servletPath) {
        // This looks at the start of the of the URL to check for resource paths in the root webapp.
        // Example: exclusions = ['/images','/css','/js,'/favicon.ico']
        //         servletPath = /images/bootstrap/cursor.gif
        //
        // It must not interfere with substrings, like so:
        //         servletPath = /json
        // This starts with /js, which is a part of the exclusions, so we need to ensure that either
        // the servletPath is exactly the same as the exclusion (like '/favicon.ico')
        // or the servletPath has higher length and the next character after the exclusion has to be a separator, 
        // so we know it is a folder to look up
        for (String path : exclusions) {
            if ( servletPath.startsWith(path) // startsWith uses its internal array without any copying (fast)
                 && (servletPath.length() == path.length() || // they are equal
                     servletPath.charAt(path.length()) == '/')) // servletPath MUST be larger, and the next char needs to be a separator
                return servletPath;
        }
        
        
        //TODO
        //Language prefix is no longer used as built-in part of the URL
        //The language needs to be explicit specified elsewhere
        /*
        // If nothing was found, then perhaps the URL had a language prefix
        int start = servletPath.indexOf('/',1);
        
        
        
        // Do not even try
        if (start < 0) return null;
        
        String segment = servletPath.substring(start);
        for (String path : exclusions) { // this is most likely faster than Arrays#binarySearch as exclusions is extremely small
            if (segment.startsWith(path))
                return segment;
        }*/
        
        // If we have to look past for more than a single URL segment
        /*int start = servletPath.indexOf('/',1), end = start;
        String segment;
        while (end > -1) { // fail fast
            segment = servletPath.substring(start);
            for (String path : exclusions) { 
                if (segment.startsWith(path))
                    return segment;
            }
            start = end;
            end = servletPath.indexOf('/',start+1);
        }*/
        return null;
    }
    
    //@Override
    public void destroy() {
        bootstrapper.shutdown();
//        appBootstrap.destroy(appContext);
    }

    @Override
    public void handle(Req request, Resp response) throws Exception {
        System.out.println(request.path());
        System.out.println(response);
    }

}
