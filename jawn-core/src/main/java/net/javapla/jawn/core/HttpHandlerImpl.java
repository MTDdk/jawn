package net.javapla.jawn.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

import net.javapla.jawn.core.exceptions.InitException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.Response;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.ServerContext;
import net.javapla.jawn.core.templates.TemplateEngine;

class HttpHandlerImpl implements HttpHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    //https://github.com/perwendel/spark/blob/master/src/main/java/spark/staticfiles/StaticFilesConfiguration.java
    private String[] exclusions;
    protected final FrameworkEngine engine;
    private final Injector injector;
    
    @Inject
    public HttpHandlerImpl(FrameworkEngine engine, Injector injector) {
        this.engine = engine;
        this.injector = injector;
        
        config();
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        if (filteringResources(response, request.path(), resource -> translateResource(resource))) return;
        
        ServerContext context = (ServerContext) injector.getInstance(Context.class);
        context.init(request, response);
        engine.onRouteRequest(context);
        
    }
    
    private void config() {
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
    }
    
    /**
     * A problem arises when the server tries to serve a resource, which does not start with an excluded path,
     * and the server just sees it as a route to handle itself, when it actually ought to send it up the chain
     * 
     * @author MTD
     * @return true, if a filtering has happened and nothing else should be done by this current dispatcher
     */
    //TODO extract to an independent Filter
    private static final boolean filteringResources(Response response, final String path, final Function<String,String> needsTranslation) throws IOException {
        String translated = needsTranslation.apply(path);
//        String translated = translateResource(path);
        if (translated != null) {
            
            // Setting default headers for static files
            // One week - Google recommendation
            // https://developers.google.com/speed/docs/insights/LeverageBrowserCaching
            response.header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800");
            File file = new File("webapp/" + translated);//TODO configurable
            if (file.canRead())
                response.header(HttpHeaders.ETAG, String.valueOf(file.lastModified()));
            
            //chain.doFilter(r, resp);
            try {
                response.send(new FileInputStream(file));
                response.end();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
   
   /**
    * Actually sorts the paths, which is not appreciated and not even used anywhere
    * @return
    */
   private Set<String> findExclusionPaths() {
       Set<String> exclusions = new TreeSet<String>();
       
       // Let other handlers deal with folders that do not reside in the WEB-INF or META-INF
       List<String> collect = null;
       File webapp = new File("webapp");
       String[] paths = webapp.list();
       if (webapp.exists() && paths != null)
           collect = Arrays.asList(paths);
       
       if (webapp.exists() && !webapp.canRead()) {
           // It means that the server cannot read files at all
           throw new InitException("HttpHandlerImpl cannot read files. Reason is unknown");
       } else if (!webapp.exists() || collect == null || collect.isEmpty()) {
           // Whenever this is empty it might just be because someone forgot to add the 'webapp' folder to the distribution
           // OR the framework is used without the need for serving files (such as views).
           logger.error("HttpHandlerImpl did not find any files in webapp - not serving any files then");
           return exclusions;
       }
       
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
   
   /*protected static final boolean redirectUrlEndingWithSlash(final String path, final HttpServletResponse response) throws IOException {
       if (path.length() > 1 && path.charAt(path.length()-1) == '/') {  //ends with
           response.sendRedirect(path.substring(0, path.length()-1));
           return true;
       }
       return false;
   }*/
   

}
