package net.javapla.jawn.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.http.SessionFacade;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.uploads.FormItem;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.HttpHeaderUtil;
import net.javapla.jawn.core.util.MultiList;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.inject.Inject;


/**
 * @author MTD
 */
class JawnServletContext implements Context.Internal {
    
    private static final String X_POWERED_BY = "X-Powered-By";
    
    
    private final PropertiesImpl properties;
    private final ParserEngineManager parserManager;
    
    private ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    private Route route;
    private String format, language;
    /**
     * Holds the actual routed path used in this (request)context.
     * This might differ from requestUri as routedPath is stripped from any language
     */
    private String routedPath;
    
    // servletcontext, appcontext (what the hell is the difference?)
    // requestcontext - the hell, man??
    @Inject
    JawnServletContext(PropertiesImpl properties, ParserEngineManager parserManager) {
        this.properties = properties;
        this.parserManager = parserManager;
    }
    
    public void init(HttpServletRequest request, HttpServletResponse response) {
        this.servletContext = request.getServletContext();
        this.request = request;
        this.response = response;
        
        // Set the encoding according to the user defined
        
        setEncoding(properties.get(Constants.DEFINED_ENCODING)); //TODO extract staticly somehow
        addResponseHeader(X_POWERED_BY, Constants.FRAMEWORK_NAME);
    }
    
    public void setRouteInformation(Route route, String format, String language, String routedPath) throws IllegalArgumentException {
        if (route == null)
            throw new IllegalArgumentException("Route could not be null");
        this.route = route;
        
        this.format = format;
        this.language = language;
        this.routedPath = routedPath;
    }
    
    /**
     * @return An instance of the Request interface
     */
    public Request createRequest() {
        return new RequestImpl(request, parserManager);
    }
    
    public SessionFacade getSession(boolean createIfNotExists) {
        HttpSession session = request.getSession(createIfNotExists);
        if (session == null) return null;
        return new SessionFacadeImpl(session);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setFlash(String name, Object value) {
        SessionFacade session = getSession(true);
        if (session.get(Context.FLASH_SESSION_KEYWORD) == null) {
            session.put(Context.FLASH_SESSION_KEYWORD, new HashMap<String, Object>());
        }
        ((Map<String, Object>) session.get(Context.FLASH_SESSION_KEYWORD)).put(name, value);
    }
    
    //README do we need some sort of globally available context?
//    public AppContext createAppContext() {
//        return new AppContext(servletContext);
//    }
    
    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    public Route getRoute() {
        return route;
    }
    public String getRouteParam(String name) {
        if (route == null) return null;
        return route.getPathParametersEncoded(routedPath).get(name);
    }
    /**
     * Returns the found language of the URI - if any.
     * If the languages are set in Bootstrap, only these are valid.
     * 
     * @return
     *      The found language if any valid are found, or if the {lang} route parameter
     *      is set - else null
     */
    public String getRouteLanguage() {
        String routeLang = getRouteParam("lang");
        if (routeLang != null) return routeLang;
        return language;
    }
    public String getRouteFormat() {
        return format;//route.getFormat();
    }
    
    
    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    public String getId(){
        /*String paramId = request.getParameter("id");
        if(paramId != null && request.getAttribute("id") != null){
//            Logger logger = LoggerFactory.getLogger(RequestUtils.class);
//            logger.warn("WARNING: probably you have 'id' supplied both as a HTTP parameter, as well as in the URI. Choosing parameter over URI value.");
        }

        String theId;
        if(paramId != null){
            theId =  paramId;
        }else{
            Object id = request.getAttribute("id");
            theId =  id != null ? id.toString() : null;
        }
        return theId;*/
        return param("id");
    }

    /**
     * Request servlet path
     * @return
     */
    public String path() {
        return request.getServletPath();
    }
    
    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    public String contextPath() {
        return request.getContextPath();
    }
    
    /**
     * Differs from {@link #requestUrl()} in that this
     * might have been stripped from language prefix
     * @return
     */
    public String getRoutedPath() {
        return routedPath;
    }
    
    /**
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    public String requestUrl(){
        return request.getRequestURL().toString();
    }
    
    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    public String requestUri(){
        return request.getRequestURI();
    }
    
    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    public String queryString(){
        return request.getQueryString();
    }
    
    public String method() {
        return request.getMethod();
    }
    
    public HttpMethod getHttpMethod() {
        return HttpMethod.getMethod(this);
    }
    
    public String requestContentType() {
        return request.getContentType();
    }
    
    public String getAcceptContentType() {
        String contentType = request.getHeader("accept");
        return HttpHeaderUtil.parseAcceptHeader(contentType);   
    }

    /**
     * Returns an instance of <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     * 
     * MTD: re-mapped the return value to accommodate a different coding style
     *
     * @return an instance <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    public MultiList<String>/*Map<String, List<String>>*/ params() {
        //MTD: deleted class SimpleHash as its only contribution was an unnecessary #toString()
        // - for the curious, it wrote: {first: [more], of: [it]} as opposed to standard: {first=[more],of=[it]}
        
        //#getParameterMap() is by default unmodifiable, so we need to re-map it
        Map<String, String[]> requestParams = request.getParameterMap();

        MultiList<String> params = new MultiList<>();
        for (Entry<String, String[]> entry : requestParams.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        
        
        if(getId() != null)
            params.put("id", getId());

        
        
        Map<String, String> routeParameters = route.getPathParametersEncoded(routedPath);//requestContext/*getRequestContext()*/.getUserSegments();
        for(Entry<String, String> entry : routeParameters.entrySet()){
            params.put(entry.getKey(), entry.getValue());
        }
        
        //MTD: added wildcard to params
//        String wildCard = requestContext.getWildCardName();
//        if (wildCard != null)
//            params.put(wildCard, requestContext.getWildCardValue());

        return params;
    }

    public Map<String, String> getResponseHeaders() {
        Collection<String> names  = response.getHeaderNames();
        Map<String, String> headers = new HashMap<String, String>();
        for (String name : names) {
            headers.put(name, response.getHeader(name));
        }
        return headers;
    }

    /**
     * Returns a String containing the real path for a given virtual path. For example, the path "/index.html" returns
     * the absolute file path on the server's filesystem would be served by a request for
     * "http://host/contextPath/index.html", where contextPath is the context path of this ServletContext.
     * 
     * <p>The real path returned will be in a form appropriate to the computer and operating system on which the servlet
     * container is running, including the proper path separators. This method returns null if the servlet container
     * cannot translate the virtual path to a real path for any reason (such as when the content is being made
     * available from a .war archive).</p>
     *
     * <p>
     * JavaDoc copied from: <a href="http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29">
     * http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29</a>
     * </p>
     *
     * @param path a String specifying a virtual path
     * @return a String specifying the real path, or null if the translation cannot be performed
     */
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }
    
    /**
     * Returns value of routing user segment, or route wild card value, or request parameter.
     * If this name represents multiple values, this  call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of routing user segment, or route wild card value, or request parameter.
     */
    public String param(String name){
        
        /*if(name.equals("id")){
            return getId();
        } else */if (request.getParameter(name) != null) {
            return request.getParameter(name);
            
            
//        }else if(requestContext.getUserSegments().get(name) != null){
//            return requestContext.getUserSegments().get(name);
//        }else if(requestContext.getWildCardName() != null
//                && name.equals(requestContext.getWildCardName())){
//            return requestContext.getWildCardValue();
        }else{
            return getRouteParam(name);
        }
    }
    
    /**
     * Returns local host name on which request was received.
     *
     * @return local host name on which request was received.
     */
    public String host() {
        return request.getLocalName();
    }
    
    public String scheme() {
        return request.getScheme();
    }
    
    public String serverName() {
        return request.getServerName();
    }
    
    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    public String ipAddress() {
        return request.getLocalAddr();
    }
    
    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    public String protocol(){
        return request.getProtocol();
    }
    
    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    public String requestHeader(String name){
        return request.getHeader(name);
    }

    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    public Map<String, String> requestHeaders(){
        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }
    
    public String[] requestParameterValues(String name) {
        return request.getParameterValues(name);
    }
    
    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    public int port(){
        return request.getLocalPort();
    }
    
    
    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    public String remoteHost(){
        return request.getRemoteHost();
    }

    /**
     * IP address of the requesting client.
     * If the IP of the request seems to come from a local proxy,
     * then the X-Forwarded-For header is returned.
     *
     * @return IP address of the requesting client.
     */
    public String remoteAddress(){
        String remoteAddr = request.getRemoteAddr();
        
        // This could be a list of proxy IPs, which the developer could
        // provide via some configuration
        if ("127.0.0.1".equals(remoteAddr)) remoteAddr = requestHeader("X-Forwarded-For");
        return remoteAddr;
    }
    
    public String getParameter(String name) {
        return request.getParameter(name);
    }
    
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        request.setCharacterEncoding(encoding);
    }
    
    public Cookie getCookie(String cookieName) {
        List<Cookie> cookies = getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        return null;
    }
    public boolean hasCookie(String cookieName) {
        return getCookie(cookieName) != null;
    }
    public List<Cookie> getCookies() {
        javax.servlet.http.Cookie[] servletCookies = request.getCookies();
        List<Cookie> cookies = new ArrayList<Cookie>();
        
        if(servletCookies != null) {
            for (javax.servlet.http.Cookie servletCookie: servletCookies) {
                Cookie cookie = CookieHelper.fromServletCookie(servletCookie);
                cookies.add(cookie);
            }
        }
        return cookies;
    }
    
    public Locale requestLocale() {
        return request.getLocale();
    }
    public void responseLocale(Locale locale) {
        response.setLocale(locale);
    }
    
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }
    public <T> T getAttribute(String name, Class<T> clazz) {
        return clazz.cast(getAttribute(name));
    }
    
/* ********
 * Upload
 ********* */
    public boolean isRequestMultiPart() {
        return ServletFileUpload.isMultipartContent(request);
    }
    
    /**
     * Gets the FileItemIterator of the input.
     * 
     * Can be used to process uploads in a streaming fashion. Check out:
     * http://commons.apache.org/fileupload/streaming.html
     * 
     * @return the FileItemIterator of the request or null if there was an
     *         error.
     */
    public Optional<List<FormItem>> parseRequestMultiPartItems(String encoding) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(properties.getInt(Constants.PROPERTY_UPLOADS_MAX_SIZE/*Constants.Params.maxUploadSize.name()*/));//Configuration.getMaxUploadSize());
        factory.setRepository(new File(System.getProperty("java.io.tmpdir"))); //Configuration.getTmpDir());
        //README the file for tmpdir *MIGHT* need to go into Properties
        
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        if(encoding != null)
            upload.setHeaderEncoding(encoding);
        upload.setFileSizeMax(properties.getInt(Constants.PROPERTY_UPLOADS_MAX_SIZE));
        
        try {
            List<FormItem> items = upload.parseRequest(request)
                    .stream()
                    .map(item -> new ApacheFileItemFormItem(item))
                    .collect(Collectors.toList());
            return Optional.of(items);
        } catch (FileUploadException e) {
            //"Error while trying to process mulitpart file upload"
//            throw new ControllerException(e);
            //README: perhaps some logging
        }
        return Optional.empty();
    }
    
    
    /**
     * Sets an attribute value.
     * <p>
     * Attributes are shared state for the duration of the request;
     * useful to pass values between {@link Filter filters} and
     * controllers.
     *
     * @see #getAttribute(String)
     * @see #getAttribute(String, Class)
     */
    public void setAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }
    
    public void setResponseStatus(int status) {
        response.setStatus(status);
    }
    
    public void setResponseContentType(String type) {
        response.setContentType(type);
    }
    
    public void responseContentLength(int length) {
        response.setContentLength(length);
    }
    
    public void addCookie(Cookie cookie) {
        response.addCookie(CookieHelper.toServletCookie(cookie));
    }
    
    public void addResponseHeader(String name, String value) {
        response.addHeader(name, value);
    }
    
    public void setResponseHeader(String name, String value) {
        response.setHeader(name, value);
    }
    
    public Collection<String> responseHeaderNames() {
        return response.getHeaderNames();
    }
    public String responseHeader(String name) {
        return response.getHeader(name);
    }
    
    /**
     * Use to send raw data to HTTP client.
     *
     * @param contentType content type
     * @param headers set of headers.
     * @param status status.
     * @return instance of output stream to send raw data directly to HTTP client.
     * @throws IOException 
     */
    /*public OutputStream outputStream(String contentType, Map<String, String> headers, int status) {
        try {
            if (headers != null) {
                for (String key : headers.keySet()) {
                    if (headers.get(key) != null)
                        response.addHeader(key.toString(), headers.get(key).toString());
                }
            }

            return response.getOutputStream();
        }catch(Exception e){
            throw new ControllerException(e);
        }
    }*/
    public OutputStream responseOutputStream() throws IOException {
        return response.getOutputStream();
    }
    public PrintWriter responseWriter() throws IOException {
        return response.getWriter();
    }
    
    public String getResponseEncoding() {
        return response.getCharacterEncoding();
    }
    /**
     * Character encoding for response
     * @param encoding
     */
    public void setEncoding(String encoding) {
        response.setCharacterEncoding(encoding);
    }
    
    
/* ****** */

    public final ResponseStream finalizeResponse(Response controllerResponse) {
        return finalizeResponse(controllerResponse, true);
    }
    
    public final ResponseStream finalizeResponse(final Response controllerResponse, boolean handleFlash) {
        // status
        response.setStatus(controllerResponse.status());
        
        // content type
        if (response.getContentType() == null && controllerResponse.contentType() != null)
            response.setContentType(controllerResponse.contentType());
        
        // encoding
        if (response.getCharacterEncoding() == null) { // encoding is already set in the controller
            if (controllerResponse.charset() != null)
                response.setCharacterEncoding(controllerResponse.charset()); // the response has an encoding
            else 
                response.setCharacterEncoding(Constants.DEFAULT_ENCODING); // use default
        }
        
        // flash
        if (handleFlash) {
            SessionFacade session = getSession(false);
            if (session != null && session.containsKey(FLASH_SESSION_KEYWORD)) {
                Object object = session.get(FLASH_SESSION_KEYWORD);
                controllerResponse.addViewObject(FLASH_KEYWORD, object);
                session.remove(FLASH_SESSION_KEYWORD);
            }
        }
        
        // copy headers
        if (!controllerResponse.headers().isEmpty()) {
           for (Entry<String, String> entry : controllerResponse.headers().entrySet()) {
               response.addHeader(entry.getKey(), entry.getValue());
           }
        }
        
        return new ResponseStreamServlet(response);
    }

}
