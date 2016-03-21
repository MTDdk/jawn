package net.javapla.jawn.server;


import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

/**
 * @author MTD
 */
public class JawnFilter extends JawnServlet implements Filter {
    private static final long serialVersionUID = -3987210815425773337L;
    
    
//    private ServletContext servletContext;
    
    

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config(filterConfig.getServletContext());
        
//        this.servletContext = filterConfig.getServletContext();
        
        
        
        // find paths inside webapp, that are NOT WEB-INF
       /* Set<String> exclusionPaths = findExclusionPaths(filterConfig.getServletContext());
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
        
        
        logger.info("Java-web-planet: starting the app in environment: " + injector.getInstance(PropertiesImpl.class).getMode());*/
    }

    


    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws ServletException, IOException {
        try {
            doFilter((HttpServletRequest) req, (HttpServletResponse) resp, chain);
        } catch (ClassCastException e) {
            // ought not be possible
            throw new ServletException("non-HTTP request or response", e);
        }
    }
    
    public final void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)  throws ServletException, IOException {

        final String path = request.getServletPath();
        
        //MTD: filtering resources
        if (filteringResources(request, response, chain, path, resource -> translateResource(resource))) return;
        
        // redirect if the path ends with a slash - URLS cannot end with a slash - they should not do such a thing - why would they?
        if (redirectUrlEndingWithSlash(path, response)) return;
        
        service(request, response);

        /*ContextInternal context = injector.getInstance(ContextInternal.class);
        context.init(servletContext, request, response);
        
        engine.runRequest(context);*/
    }
    
    /**
     * A problem arises when the server tries to serve a resource, which does not start with an excluded path,
     * and the server just sees it as a route to handle itself, when it actually ought to send it up the chain
     * 
     * @author MTD
     * @return true, if a filtering has happened and nothing else should be done by this current dispatcher
     */
    //TODO extract to an independent Filter
    protected static final boolean filteringResources(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain, final String path, final Function<String,String> needsTranslation) throws ServletException, IOException {
        String translated = needsTranslation.apply(path);
//        String translated = translateResource(path);
        if (translated != null) {
//            logger.debug("URI excluded: {}", path);
            
            HttpServletRequest r = req;
            if (path.length() != translated.length()) {//!path.startsWith(translated)) {
//                int indexOfExcluded = path.indexOf(translated);
//                String t = path.substring(indexOfExcluded);
                r = createServletRequest(req, translated);
//                logger.debug(".. and translated -> {}", translated);
            }
            
            
            // Setting default headers for static files
            // One week - Google recommendation
            // https://developers.google.com/speed/docs/insights/LeverageBrowserCaching
            resp.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=604800");
            File file = new File(req.getServletContext().getRealPath(translated));
            if (file.canRead())
                resp.setHeader(HttpHeaders.ETAG, String.valueOf(file.lastModified()));
            
            chain.doFilter(r, resp);
            return true;
        }
        return false;
    }
    
    /**
     * Creates a new HttpServletRequest object.
     * Useful, as we cannot modify an existing ServletRequest.
     * Used when resources needs to have the {controller} stripped from the servletPath.
     * 
     * @author MTD
     */
    private final static HttpServletRequest createServletRequest(final HttpServletRequest req, final String translatedPath) {
        return new HttpServletRequestWrapper(req){
            @Override
            public String getServletPath() {
                return translatedPath;
            }
        };
    }


    /**/

}
