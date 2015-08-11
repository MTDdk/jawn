package net.javapla.jawn.core.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.spi.Filter;
import net.javapla.jawn.core.util.MultiList;

import org.apache.commons.fileupload.FileItem;

public interface Context {
    
    static final String FLASH_KEYWORD = "flash";
    static final String FLASH_SESSION_KEYWORD = Context.class.getName() + ".flash";
    
    /**
     * Internal contract goes here.
     * <p>
     * Not visible for users
     */
    interface Internal extends Context {
        public void setRouteInformation(Route route, String format, String language, String routedPath) throws IllegalArgumentException;
    }
    
    /**
     * @return An instance of the Request interface
     */
    public Request createRequest();
    
    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    public Route getRoute();
    
    /**
     * Returns the found language of the URI - if any.
     * If the languages are set in Bootstrap, only these are valid.
     * 
     * @return
     *      The found language if any valid are found, or if the {lang} route parameter
     *      is set - else null
     */
    public String getRouteLanguage();
    public String getRouteFormat();
    
    public String getRouteParam(String name);
    
    public SessionFacade getSession(boolean createIfNotExists);
    public void setFlash(String name, Object value);
    
/* *************** */
/*   REQUEST       */
/* *************** */
    
    
    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    public String contextPath();
    
    /**
     * Request servlet path
     * @return
     */
    public String path();
    
    /**
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    public String requestUrl();
    
    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    public String requestUri();
    
    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    public String queryString();
    
    public String method();
    public HttpMethod getHttpMethod();
    
    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    public int port();
    
    /**
     * Returns local host name on which request was received.
     *
     * @return local host name on which request was received.
     */
    public String host();
    
    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    public String ipAddress();
    
    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    public String protocol();
    
    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    public String remoteHost();
    
    /**
     * IP address of the requesting client.
     *
     * @return IP address of the requesting client.
     */
    public String remoteAddress();
    
    public String getAcceptContentType();
    public String requestContentType();
    
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
    public String getRealPath(String path);
    
    
    
    /**
     * Returns an instance of <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     * 
     * MTD: re-mapped the return value to accommodate a different coding style
     *
     * @return an instance <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    public MultiList<String> params();
    
    public String param(String name);
    
    public String getParameter(String name);
    
    public Object getAttribute(String name);
    public <T> T getAttribute(String name, Class<T> clazz);
    
    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    public Map<String, String> requestHeaders();
    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    public String requestHeader(String name);
    public String[] requestParameterValues(String name);
    
    public Locale requestLocale();
    
    public Cookie getCookie(String cookieName);
    public boolean hasCookie(String cookieName);
    public List<Cookie> getCookies();
    
    
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
    void setAttribute(String name, Object value);
    
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException;
    
    
    public boolean isRequestMultiPart();
    /**
     * Gets the FileItemIterator of the input.
     * 
     * Can be used to process uploads in a streaming fashion. Check out:
     * http://commons.apache.org/fileupload/streaming.html
     * 
     * @return the FileItemIterator of the request or null if there was an
     *         error.
     */
    public List<FileItem> parseRequestMultiPartItems(String encoding);
    
/* *************** */
/*   RESPONSE      */
/* *************** */
    
    public String getResponseEncoding();
    /**
     * Character encoding for response
     * @param encoding
     */
    public void setEncoding(String encoding);
    
    public void responseLocale(Locale locale);
    
    public void addCookie(Cookie cookie);
    
    public void addResponseHeader(String name, String value);
    public Collection<String> responseHeaderNames();
    
    
    
    public PrintWriter responseWriter() throws IOException;
    /**
     * Use to send raw data to HTTP client.
     *
     * @param contentType content type
     * @param headers set of headers.
     * @param status status.
     * @return instance of output stream to send raw data directly to HTTP client.
     * @throws IOException 
     */
    public OutputStream responseOutputStream() throws IOException;
    
    
/* ****** */

    public ResponseStream finalizeResponse(Response controllerResponse);
    public ResponseStream finalizeResponse(Response controllerResponse, boolean handleFlash);
}
