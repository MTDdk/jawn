/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package net.javapla.jawn;

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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.util.Constants;
import net.javapla.jawn.util.MultiList;


/**
 * @author MTD
 */
public class Context {
    
//    private final Map<String, Object> viewValues; //TODO these needs to be a part of the "Controller"Response instead
    
    private final ServletContext servletContext;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final PropertiesImpl properties;
    
//    private ControllerResponse controllerResponse;
    private NewControllerResponse newControllerResponse;
    
    private NewRoute route;
    private String format, language;
    /**
     * Holds the actual routed path used in this (request)context.
     * This might differ from requestUri as routedPath is stripped from any language
     */
    private String routedPath;
    
    // servletcontext, appcontext (what the hell is the difference?)
    // requestcontext - the hell, man??
    public Context(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, PropertiesImpl properties) {
        this.servletContext = servletContext;
        this.request = request;//new RequestImpl(request);
        this.response = response;
        this.properties = properties;
        
//        this.viewValues = new HashMap<>();
        this.newControllerResponse = NewControllerResponseBuilder.ok().contentType(MediaType.TEXT_HTML);
    }
    
    public void init(NewRoute route/*, RequestContext requestContext*/, String format, String language, String routedPath) {
        if (route == null)
            throw new IllegalArgumentException("Route could not be null");

        this.route = route;
        
        this.format = format;
        this.language = language;
        this.routedPath = routedPath;
    }
    
    
    void setNewControllerResponse(NewControllerResponse response) {
        newControllerResponse = response;
    }
//    void setControllerResponse(ControllerResponse response) {
//        //TODO perhaps some copying of viewvalues when setting a new response (just in case) 
//        controllerResponse = response;
//    }
    NewControllerResponse getNewControllerResponse() {
        return newControllerResponse;
    }
//    ControllerResponse getControllerResponse() {
//        return controllerResponse;
//    }
    
    /**
     * @return An instance of the Request interface
     */
    public Request createRequest() {
        return new RequestImpl(request);
    }
    
    public SessionFacade getSession() {
        return new SessionFacade(request);
    }
    
    public AppContext createAppContext() {
        return new AppContext(servletContext);
    }
    
    //TODO this somehow needs to be a part of the ControllerResponse, which, of course, is not
    //exactly trivial
    //Perhaps creating a ControllerResponse on the fly for TemplateRendering - if it gets overridden by
    //a builder of sorts, the ControllerResponse is simply discarded
//    public void addViewObject(String name, Object value) {
//        viewValues.put(name, value);
//    }
//    public Map<String, Object> getViewObjects() {
//        return viewValues;
//    }
    
    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    public NewRoute getRoute() {
        return route;
    }
    public String getRouteParam(String name) {
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
        if (language != null)
            return language;//route.getLanguage();
        return getRouteParam("lang");
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
    
    public String requestContentType() {
        return request.getContentType();
    }
    
    public String getAcceptContentType() {
        String contentType = request.getHeader("accept");

        if (contentType == null) {
            return MediaType.TEXT_HTML;
        }

        if (contentType.indexOf("application/xhtml") != -1
                || contentType.indexOf("text/html") != -1
                || contentType.startsWith("*/*")) {
            return MediaType.TEXT_HTML;
        }

        if (contentType.indexOf("application/xml") != -1
                || contentType.indexOf("text/xml") != -1) {
            return MediaType.APPLICATION_XML;
        }

        if (contentType.indexOf("application/json") != -1
                || contentType.indexOf("text/javascript") != -1) {
            return MediaType.APPLICATION_JSON;
        }

        if (contentType.indexOf("text/plain") != -1) {
            return MediaType.TEXT_PLAIN;
        }

        if (contentType.indexOf("application/octet-stream") != -1) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        if (contentType.endsWith("*/*")) {
            return MediaType.TEXT_HTML;
        }

        return MediaType.TEXT_HTML;
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
     *
     * @return IP address of the requesting client.
     */
    public String remoteAddress(){
        return request.getRemoteAddr();
    }
    
    public String getParameter(String name) {
        return request.getParameter(name);
    }
    
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        request.setCharacterEncoding(encoding);
    }
    
    public Cookie getCookie(String cookieName) {
        List<Cookie> cookies = getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
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
                Cookie cookie = Cookie.fromServletCookie(servletCookie);
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
    public List<FileItem> parseRequestMultiPartItems(String encoding) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(properties.getInt(Constants.Params.maxUploadSize.name()));//Configuration.getMaxUploadSize());
        factory.setRepository(new File(System.getProperty("java.io.tmpdir"))); //Configuration.getTmpDir());
        //README the file for tmpdir *MIGHT* need to go into Properties
        
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        if(encoding != null)
            upload.setHeaderEncoding(encoding);
        upload.setFileSizeMax(properties.getInt(Constants.Params.maxUploadSize.name()));//Configuration.getMaxUploadSize());
        
        try {
            return upload.parseRequest(request);
        } catch (FileUploadException e) {
            //"Error while trying to process mulitpart file upload"
//            throw new ControllerException(e);
            //README: perhaps some logging
            return null;
        }
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
        response.addCookie(Cookie.toServletCookie(cookie));
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
    public OutputStream responseOutputStream() {
        try {
            return response.getOutputStream();
        } catch (IOException e) {
            throw new ControllerException(e);
        }
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
    
    /**
     * 
     * @param path
     * @throws IOException If the response was committed or if a partial URL is given and cannot be converted into a valid URL
     * @see HttpServletResponse#sendRedirect(String)
     */
    public void responseSendRedirect(String path) throws IOException {
        response.sendRedirect(path);
    }
    
    
/* ****** */

    public ResponseStream finalize(NewControllerResponse controllerResponse) {
        // status
        response.setStatus(controllerResponse.status());
        
        // content type
        if (controllerResponse.contentType() != null)
            response.setContentType(controllerResponse.contentType());
        
        // encoding
        if (controllerResponse.charset() != null)
            response.setCharacterEncoding(controllerResponse.charset());
        else 
            response.setCharacterEncoding(Constants.ENCODING);
        
        
        // copy headers
        if (!controllerResponse.headers().isEmpty()) {
           for (Entry<String, String> entry : controllerResponse.headers().entrySet()) {
               response.addHeader(entry.getKey(), entry.getValue());
           }
        }
        
        //cookies
        
        return new ResponseStreamServlet(response);
    }

}
