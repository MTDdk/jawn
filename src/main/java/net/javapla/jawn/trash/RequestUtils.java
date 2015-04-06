package net.javapla.jawn.trash;

import java.util.ArrayList;
import java.util.List;

import net.javapla.jawn.FormItem;

/**
 * 
 * TODO: this needs to become a default interface, once we move the project to java 8
 * 
 * @author igor, on 6/16/14.
 */
@Deprecated
class RequestUtils {


    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    /*public static String getId(){
        String paramId = Context.getHttpRequest().getParameter("id");
        if(paramId != null && Context.getHttpRequest().getAttribute("id") != null){
            Logger logger = LoggerFactory.getLogger(RequestUtils.class);
            logger.warn("WARNING: probably you have 'id' supplied both as a HTTP parameter, as well as in the URI. Choosing parameter over URI value.");
        }

        String theId;
        if(paramId != null){
            theId =  paramId;
        }else{
            Object id = Context.getHttpRequest().getAttribute("id");
            theId =  id != null ? id.toString() : null;
        }
        return StringUtil.blank(theId) ? null : theId;
    }*/


    /**
     * Returns a format part of the URI, or null if URI does not have a format part.
     * A format part is defined as part of URI that is trailing after a last dot, as in:
     *
     * <code>/books.xml</code>, here "xml" is a format.
     *
     * @return format part of the URI, or nul if URI does not have it.
     */
//    public static String format(){
//        return Context.getFormat();
//    }


    /**
     * Returns instance of {@link AppContext}.
     *
     * @return instance of {@link AppContext}.
     */
//    public static AppContext appContext(){
//        return Context.getAppContext();
//    }


    


    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
//    public static Route getRoute(){
//        return Context.getRoute();
//    }


    /**
     * Tests if a request parameter exists. Disregards the value completely - this
     * can be empty string, but as long as parameter does exist, this method returns true.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     *//*
    public static boolean exists(String name){
        return param(name) != null;
    }*/

    /**
     * Synonym of {@link #exists(String)}.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     *//*
    public static boolean requestHas(String name){
        return param(name) != null;
    }*/


    /**
     * Returns local host name on which request was received.
     *
     * @return local host name on which request was received.
     *//*
    public static String host() {
        return Context.getHttpRequest().getLocalName();
    }*/


    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     *//*
    public static  String ipAddress() {
        return Context.getHttpRequest().getLocalAddr();
    }*/





    /**
     * This method returns a protocol of a request to web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Proto</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #protocol()} method is used.
     *
     * @return protocol of web server request if <code>X-Forwarded-Proto</code> header is found, otherwise current
     * protocol.
     *//*
    public static String getRequestProtocol(){
        String protocol = header("X-Forwarded-Proto");
        return StringUtil.blank(protocol)? protocol(): protocol;
    }*/

    



    


    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     *//*
    public static String protocol(){
        return Context.getHttpRequest().getProtocol();
    }*/

    


    /**
     * Returns value of routing user segment, or route wild card value, or request parameter.
     * If this name represents multiple values, this  call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of routing user segment, or route wild card value, or request parameter.
     */
    /*public static String param(String name){
        if(name.equals("id")){
            return getId();
        }else if(Context.getRequestContext().getUserSegments().get(name) != null){
            return Context.getRequestContext().getUserSegments().get(name);
        }else if(Context.getRequestContext().getWildCardName() != null
                && name.equals(Context.getRequestContext().getWildCardName())){
            return Context.getRequestContext().getWildCardValue();
        }else{
            return Context.getHttpRequest().getParameter(name);
        }
    }*/
    
    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    /*public static List<String> params(String name){
        if (name.equals("id")) {
            String id = getId();
            return id != null ? asList(id) : Collections.emptyList();
        } else {
            String[] values = Context.getHttpRequest().getParameterValues(name);
            List<String>valuesList = values == null? new ArrayList<>() : Arrays.asList(values);
            String userSegment = Context.getRequestContext().getUserSegments().get(name);
            if(userSegment != null){
                valuesList.add(userSegment);
            }
            return valuesList;
        }
    }*/
    
    /**
     * Convenience method to get parameter values in case <code>multipart/form-data</code> request was used.
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return multiple request values for a name. Will ignore files, and only return form fields.
     */
    public static List<String> params(final String name, final List<FormItem> formItems) {
        List<String> vals = new ArrayList<String>();
        for (FormItem formItem : formItems) {
            if(formItem.isFormField() && name.equals(formItem.getFieldName())){
                vals.add(formItem.getStreamAsString());
            }
        }
        return vals;
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
    /*public static MultiList<String>Map<String, List<String>> params() {
        //MTD: deleted class SimpleHash as its only contribution was an unnecessary #toString()
        // - for the curious, it wrote: {first: [more], of: [it]} as opposed to standard: {first=[more],of=[it]}
        
        //#getParameterMap() is by default unmodifiable, so we need to re-map it
        Map<String, String[]> requestParams = Context.getHttpRequest().getParameterMap();

        MultiList<String> params = new MultiList<>();
        for (Entry<String, String[]> entry : requestParams.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        
        
        if(getId() != null)
            params.put("id", getId());

        Map<String, String> userSegments = Context.getRequestContext().getUserSegments();

        for(String name:userSegments.keySet()){
            params.put(name, userSegments.get(name));
        }
        
        //MTD: added wildcard to params
        String wildCard = Context.getRequestContext().getWildCardName();
        if (wildCard != null)
            params.put(wildCard, Context.getRequestContext().getWildCardValue());

        return params;
    }*/
    
    
    /**
     * Convenience method to get parameters in case <code>multipart/form-data</code> request was used.
     *
     * Returns a {@link MultiList} where keys are names of all parameters.
     *
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return a {@link MultiList} where keys are names of all parameters.
     *//*
    public static MultiList<String> params(List<FormItem> formItems) {
        MultiList<String> params = new MultiList<>();
        for (FormItem formItem : formItems) {
            if(formItem.isFormField() && !params.contains(formItem.getFieldName())){
                params.put(formItem.getFieldName(), formItem.getStreamAsString());
            }
        }
        return params;
    }*/



    /**
     * Converts the request input into an object of the specified class in case of <code>application/json</code> request.
     *  
     * @param clazz A representation of the expected JSON
     * @return The object of the converted JSON, or <code>throws</code> if the JSON could not be correctly deserialized,
     *         or the media type was incorrect. 
     * @throws ParsableException If the parsing from JSON to class failed
     * @throws MediaTypeException If the mediatype of the request was not "application/json"
     * @author MTD
     */
//    public static <T> T json(Class<T> clazz) throws MediaTypeException, ParsableException {
//        HttpServletRequest request = Context.getHttpRequest();
//        String contentType = request.getContentType();
//        
//        if (!MediaType.APPLICATION_JSON.equals(contentType))
//            throw new MediaTypeException("Media type was not: " + MediaType.APPLICATION_JSON);
//        
//        try (InputStream stream = request.getInputStream()) {
//            return JsonParser.parseObject(stream, clazz);
//        } catch (IOException e) {
//            throw new ParsableException(clazz);
//        }
//    }
    
    
    /**
     * Conveniently converts any input in the request into a string
     * 
     * @return Input from the request as string
     * @throws ParsableException Reading the input failed
     * @author MTD
     */
//    public static String inputStreamAsString() throws ParsableException {
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Context.getHttpRequest().getInputStream()))) {
//            return reader.lines().collect(Collectors.joining());
//        } catch (IOException e) {
//            throw new ParsableException("Reading the input failed");
//        }
//    }
    
    /**
     * Use to send raw data to HTTP client.
     *
     * @param contentType content type
     * @param headers set of headers.
     * @param status status.
     * @return instance of output stream to send raw data directly to HTTP client.
     */
//    public static OutputStream outputStream(String contentType, Map<String, String> headers, int status) {
//        try {
//            if (headers != null) {
//                for (String key : headers.keySet()) {
//                    if (headers.get(key) != null)
//                        Context.getHttpResponse().addHeader(key.toString(), headers.get(key).toString());
//                }
//            }
//
//            return Context.getHttpResponse().getOutputStream();
//        }catch(Exception e){
//            throw new ControllerException(e);
//        }
//    }

    
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
//    public static String getRealPath(String path) {
//        return Context.getServletContext().getRealPath(path);
//    }


    /**
     * Returns locale of request.
     *
     * @return locale of request.
     */
//    public static Locale locale(){
//        return Context.getHttpRequest().getLocale();
//    }

    
    /**
     * Returns collection of all cookies browser sent.
     *
     * @return collection of all cookies browser sent.
     */
//    public static List<Cookie> cookies(){
//        javax.servlet.http.Cookie[] servletCookies = Context.getHttpRequest().getCookies();
//        if(servletCookies == null)
//            return new ArrayList<Cookie>();
//
//        List<Cookie> cookies = new ArrayList<Cookie>();
//        for (javax.servlet.http.Cookie servletCookie: servletCookies) {
//            Cookie cookie = Cookie.fromServletCookie(servletCookie);
//            cookies.add(cookie);
//        }
//        return cookies;
//    }

    /**
     * Returns a cookie by name, null if not found.
     *
     * @param name name of a cookie.
     * @return a cookie by name, null if not found.
     */
//    public static Cookie cookie(String name){
//        javax.servlet.http.Cookie[] servletCookies = Context.getHttpRequest().getCookies();
//        if (servletCookies != null) {
//            for (javax.servlet.http.Cookie servletCookie : servletCookies) {
//                if (servletCookie.getName().equals(name)) {
//                    return Cookie.fromServletCookie(servletCookie);
//                }
//            }
//        }
//        return null;
//    }


    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
//    public static String cookieValue(String name){
//        return cookie(name).getValue();
//    }
//
//
//    /**
//     * Returns a path of the request. It does not include protocol, host, port or context. Just a path.
//     * Example: <code>/controller/action/id</code>
//     *
//     * @return a path of the request.
//     */
//    public static String path(){
//        return Context.getHttpRequest().getServletPath();
//    }
//
//    /**
//     * Returns a full URL of the request, all except a query string.
//     *
//     * @return a full URL of the request, all except a query string.
//     */
//    public  static String url(){
//        return Context.getHttpRequest().getRequestURL().toString();
//    }
//
//    /**
//     * Returns query string of the request.
//     *
//     * @return query string of the request.
//     */
//    public  static String queryString(){
//        return Context.getHttpRequest().getQueryString();
//    }
//
//    /**
//     * Returns an HTTP method from the request.
//     *
//     * @return an HTTP method from the request.
//     */
//    public static String method(){
//        return Context.getHttpRequest().getMethod();
//    }

    /**
     * True if this request uses HTTP GET method, false otherwise.
     *
     * @return True if this request uses HTTP GET method, false otherwise.
     */
//    public static boolean isGet() {
//        return isMethod("get");
//    }


    /**
     * True if this request uses HTTP POST method, false otherwise.
     *
     * @return True if this request uses HTTP POST method, false otherwise.
     */
//    public static boolean isPost() {
//        return isMethod("post");
//    }


    /**
     * True if this request uses HTTP PUT method, false otherwise.
     *
     * @return True if this request uses HTTP PUT method, false otherwise.
     */
//    public static boolean isPut() {
//        return isMethod("put");
//    }


    /**
     * True if this request uses HTTP DELETE method, false otherwise.
     *
     * @return True if this request uses HTTP DELETE method, false otherwise.
     */
//    public static boolean isDelete() {
//        return isMethod("delete");
//    }


//    public static boolean isMethod(String method){
//        return HttpMethod.getMethod(Context.getHttpRequest()).name().equalsIgnoreCase(method);
//    }


    /**
     * True if this request uses HTTP HEAD method, false otherwise.
     *
     * @return True if this request uses HTTP HEAD method, false otherwise.
     */
//    public static boolean isHead() {
//        return isMethod("head");
//    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
//    public static String context(){
//        return Context.getHttpRequest().getContextPath();
//    }

    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
//    public static String uri(){
//        return Context.getHttpRequest().getRequestURI();
//    }

    



    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     *//*
    public static String header(String name){
        return Context.getHttpRequest().getHeader(name);
    }

    *//**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     *//*
    public static Map<String, String> headers(){
        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> names = Context.getHttpRequest().getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, Context.getHttpRequest().getHeader(name));
        }
        return headers;
    }*/
}
