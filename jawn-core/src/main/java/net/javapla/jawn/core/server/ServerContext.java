package net.javapla.jawn.core.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.inject.Inject;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.FlashScope;
import net.javapla.jawn.core.http.FormItem;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.Response;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.http.Session;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.MultiList;

public class ServerContext implements Context.Internal2 {
    
	private static final String X_POWERED_BY = "X-Powered-By";
    
    
    private final JawnConfigurations properties;
    private final Session session;
    private final FlashScope flash;
    
    private HashMap<String, Object> contextAttributes;
    private Request request;
    private Response response;
    private volatile ResponseStream responseCreated = null;
    
    private Route route;
    /**
     * Holds the actual routed path used in this (request)context.
     * This might differ from requestUri as routedPath is stripped from any language
     */
    private String routedPath;

    private Map<String, Cookie> cookies;


    
    
    @Inject
    ServerContext(JawnConfigurations properties, Session session, FlashScope flash) {
        this.properties = properties;
        this.session = session;
        this.flash = flash;
    }
    
    public void init(Request request, Response response) {
        this.request = request;
        this.response = response;
        
        setHeader(X_POWERED_BY, Constants.FRAMEWORK_NAME);
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
    public Session getSession(/*boolean createIfNotExists*/) {
//        if (createIfNotExists && !session.isInitialised()) {
//            session.init(this);
//        } else if (!createIfNotExists && !session.isInitialised()) {
//            return null;
//        }
//        
//        return session;//sessionManger.create();
        if (!session.isInitialised()) {
            session.init(this);
        }
        return session;
    }
    
    @Override
    public FlashScope getFlash() {
        if (!flash.isInitialised()) {
            flash.init(this);
        }
        return flash;
    }

    /*@Override
    public void setFlash(String name, String value) {
        
        
        
        Session session = getSession(true);
        if (session.get(Context.FLASH_SESSION_KEYWORD) == null) {
            session.put(Context.FLASH_SESSION_KEYWORD, new HashMap<String, Object>());
        }
        session.get(Context.FLASH_SESSION_KEYWORD, Map.class).put(name, value);
    }*/
    
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
        return request.contextPath();
    }

    @Override
    public String path() {
        return request.path();
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
    @Override
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
    public void setAttribute(String name, Object value) {
        instantiateContextAttributes();
        contextAttributes.put(name, value);
    }

    @Override
    public final Object getAttribute(String name) {
        if (contextAttributes == null) return null;
        return contextAttributes.get(name);
    }

    @Override
    public <T> T getAttribute(String name, Class<T> type) throws ClassCastException {
        Object o = getAttribute(name);
        return o == null? null : type.cast(o);
    }

    @Override
    public MultiList<String> requestHeaders() {
        List<String> names = request.headerNames();
        MultiList<String> list = new MultiList<>();
        names.forEach(name -> list.put(name, request.headers(name)));
        return list;
    }

    @Override
    public String requestHeader(String name) {
        return request.header(name).orElse(null);
    }

    @Override
    public Cookie getCookie(String cookieName) {
        return getCookies().get(cookieName);
    }

    @Override
    public boolean hasCookie(String cookieName) {
        return getCookie(cookieName) != null;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        if (cookies == null) {
            cookies = request.cookies();
        }
        return cookies;
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
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public void setHeader(String name, String value) {
        response.header(name, value);
    }
    
    @Override
    public void setHeaders(String name, List<String> values) {
        response.header(name, values);
    }
    
    @Override
    public void removeHeader(String name) {
        response.removeHeader(name);
    }

    @Override
    public Writer responseWriter(Result result) throws IOException {
        return readyResponse(result, false).getWriter();
    }

    @Override
    public OutputStream responseOutputStream(Result result) throws IOException {
        return readyResponse(result, false).getOutputStream();
    }

    @Override
    public ResponseStream readyResponse(Result controllerResponse) {
        return readyResponse(controllerResponse, true);
    }

    @Override
    public /*synchronized */ResponseStream readyResponse(Result controllerResponse, boolean handleFlashAndSession) {
        if (responseCreated != null) return responseCreated;
        if (controllerResponse == null) return responseCreated = new ServerResponseStream(response);
        
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
        
        // flash and session cookies
        if (handleFlashAndSession) {
            FlashScope flash = getFlash();
            Map<String, String> currentFlashCookieData = flash.getCurrentFlashCookieData();
            if (!currentFlashCookieData.isEmpty()) {
                controllerResponse.addViewObject(FLASH_KEYWORD, currentFlashCookieData);
            }
            flash.save(this);
            
            if (session.isInitialised())
                session.save(this);
        }
        
        // copy headers
        if (!controllerResponse.headers().isEmpty()) {
           for (Entry<String, String> entry : controllerResponse.headers().entrySet()) {
               response.header(entry.getKey(), entry.getValue());
           }
        }
        
        return responseCreated = new ServerResponseStream(response);
    }
    
    private void instantiateContextAttributes() { // synchronized *should* not be needed as a request should be executed non-parallel
        if (contextAttributes == null)
            contextAttributes = new HashMap<>(5);
    }

}
