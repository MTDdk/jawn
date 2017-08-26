package net.javapla.jawn.core.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.inject.Inject;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.FormItem;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.RequestConvert;
import net.javapla.jawn.core.http.Response;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.http.SessionFacade;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.MultiList;

public class ServerContext implements Context.Internal2 {
    
	private static final String X_POWERED_BY = "X-Powered-By";
    
    
    private final JawnConfigurations properties;
    //private final SessionManager sessionManager;
//    private final Session session;
    
    private Request request;
    private Response response;
    
    private Route route;
    /**
     * Holds the actual routed path used in this (request)context.
     * This might differ from requestUri as routedPath is stripped from any language
     */
    private String routedPath;


    
    
    @Inject
    ServerContext(JawnConfigurations properties/*, Session session*/) {
        this.properties = properties;
//        this.session = session;
    }
    
    public void init(Request request, Response response) {
        this.request = request;
        this.response = response;
        
        addResponseHeader(X_POWERED_BY, Constants.FRAMEWORK_NAME);
        
        // init session scope
//        session.init(this);
    }
    
    @Override
    public void setRouteInformation(Route route, String routedPath) throws IllegalArgumentException {
        if (route == null)
            throw new IllegalArgumentException("Route could not be null");
        this.route = route;
        
        this.routedPath = routedPath;
    }
    
    @Override
    public Request request() {
        return request;
    }
    @Override
    public Response response() {
        return response;
    }

    @Override
    public RequestConvert createRequest() {
        return null;
    }

    @Override
    public Route getRoute() {
        return route;
    }

    @Override
    public String getRouteParam(String name) {
        if (route == null) return null;
        return route.getPathParametersEncoded(routedPath).get(name);
    }
    @Override
    public Map<String, String> getRouteParams() {
        if (route == null) return Collections.emptyMap();
        return route.getPathParametersEncoded(routedPath);
    }

    @Override
    public SessionFacade getSession(boolean createIfNotExists) {
        return null;//sessionManger.create();/get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setFlash(String name, Object value) {
        SessionFacade session = getSession(true);
        if (session.get(Context.FLASH_SESSION_KEYWORD) == null) {
            session.put(Context.FLASH_SESSION_KEYWORD, new HashMap<String, Object>());
        }
        ((Map<String, Object>) session.get(Context.FLASH_SESSION_KEYWORD)).put(name, value);
    }
    
    @Override
    public Modes mode() {
        return properties.getMode();
    }

    @Override
    public String contextPath() {
        // will most often be "/" and shall therefore be
        // translated to "".
        // this is set when starting the server, and probably
        // *should* be up to the user
        return "";
    }

    @Override
    public String path() {
        return request.path();
    }

    @Override
    @Deprecated
    public String requestUrl() {
        return null;
    }

    @Override
    @Deprecated
    public String requestUri() {
        return null;
    }

    @Override
    public String queryString() {
        return request.queryString();
    }

    @Override
    @Deprecated
    public String method() {
        return null;
    }

    @Override
    public HttpMethod httpMethod() {
        return request.method();
    }

    @Override
    public int port() {
        return request.port();
    }

    @Override
    @Deprecated
    public String host() {
        return null;
    }

    @Override
    public String scheme() {
        return request.scheme();
    }

    @Override
    public String serverName() {
        return null;
    }

    @Override
    @Deprecated
    public String ipAddress() {
        return request.ip();
    }

    @Override
    public String protocol() {
        return request.protocol();
    }

    @Override
    public String remoteHost() {
        return null;
    }

    /**
     * IP address of the requesting client.
     * If the IP of the request seems to come from a local proxy,
     * then the X-Forwarded-For header is returned.
     *
     * @return IP address of the requesting client.
     */
    public String remoteIP(){
        String remoteAddr = request.ip();
        
        // This could be a list of proxy IPs, which the developer could
        // provide via some configuration
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr)) 
            remoteAddr = Optional.ofNullable(requestHeader("X-Forwarded-For")).orElse("localhost");
        return remoteAddr;
    }

    @Override
    public String getAcceptContentType() {
        return null;
    }

    @Override
    public String requestContentType() {
        return request.header("Content-Type").orElse(null);
    }

    /*@Override
    public String getRealPath(String path) {
    	if (path == null) return null;
        return WEBAPP + path;
    }*/

    @Override
    public MultiList<String> params() {
        return request.params().orElse(() -> new MultiList<String>(getRouteParams()));
    }

    @Override
    public String param(String name) {
        return request.param(name).orElse(getRouteParam(name));
    }

    @Override
    public String getParameter(String name) {
        return request.param(name).orElse(null);
    }

    @Override
    @Deprecated
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    @Deprecated
    public <T> T getAttribute(String name, Class<T> clazz) {
        return null;
    }

    @Override
    public Map<String, String> requestHeaders() {
        return null;//TODO
    }

    @Override
    public String requestHeader(String name) {
        return request.header(name).orElse(null);
    }

    @Override
    public String[] requestParameterValues(String name) {
        return null;
    }

    @Override
    public Locale requestLocale() {
        return null;
    }

    @Override
    @Deprecated
    public Cookie getCookie(String cookieName) {
        //could as well just be in the Controller
        for (Cookie c : request.cookies()) {
            if (c.getName().equals(cookieName)) return c;
        }
        return null;
    }

    @Override
    @Deprecated
    public boolean hasCookie(String cookieName) {
        return false;
    }

    @Override
    public List<Cookie> getCookies() {
        return request.cookies();
    }

    @Override
    public void setAttribute(String name, Object value) {
        //deprecate?
    }

    @Override
    @Deprecated
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
    }

    @Override
    public boolean isRequestMultiPart() {
        if (httpMethod() != HttpMethod.POST) {
            return false;
        }
        String contentType = requestContentType();
        if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase().startsWith("multipart/")) {
            return true;
        }
        return false;
    }

    @Override
    public Optional<List<FormItem>> parseRequestMultiPartItems(String encoding) {
        return Optional.of(request.files());
    }
    
    @Override
    public InputStream requestInputStream() throws IOException {
        return request.in();
    }

    @Override
    public String getResponseEncoding() {
        return response.characterEncoding().map(ct -> ct.name()).orElse(null);
    }

    @Override
    public void setEncoding(String encoding) {
        response.characterEncoding(encoding);
    }

    @Override
    @Deprecated
    public void responseLocale(Locale locale) {
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public void addResponseHeader(String name, String value) {
        response.header(name, value);
    }

    @Override
    public Collection<String> responseHeaderNames() {
        //TODO
        return null;
    }

    @Override
    @Deprecated
    public PrintWriter responseWriter() throws IOException {
        return null;
    }

    @Override
    @Deprecated
    public OutputStream responseOutputStream() throws IOException {
        return null;
    }

    @Override
    public ResponseStream readyResponse(Result controllerResponse) {
        return readyResponse(controllerResponse, true);
    }

    @Override
    public ResponseStream readyResponse(Result controllerResponse, boolean handleFlash) {
        // status
        response.statusCode(controllerResponse.status());
        
        // content type
        if (response.contentType() == null && controllerResponse.contentType() != null)
            response.contentType(controllerResponse.contentType());
        
        // encoding
        if (!response.characterEncoding().isPresent()) { // encoding is already set in the controller
            if (controllerResponse.charset() != null)
                response.characterEncoding(controllerResponse.charset()); // the response has an encoding
            else 
                response.characterEncoding(Constants.DEFAULT_ENCODING); // use default
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
               response.header(entry.getKey(), entry.getValue());
           }
        }
        
        return new ServerResponseStream(response);
    }

}
