package net.javapla.jawn.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.inject.Inject;

import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Req;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.Resp;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.http.SessionFacade;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.uploads.FormItem;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.MultiList;

public class ServerContext implements Context.Internal2 {
    
private static final String X_POWERED_BY = "X-Powered-By";
    
    
    private final PropertiesImpl properties;
    private final ParserEngineManager parserManager;
    
    private Req request;
    private Resp response;
    
    private Route route;
    private String format, language;
    /**
     * Holds the actual routed path used in this (request)context.
     * This might differ from requestUri as routedPath is stripped from any language
     */
    private String routedPath;
    
    @Inject
    ServerContext(PropertiesImpl properties, ParserEngineManager parserManager) {
        this.properties = properties;
        this.parserManager = parserManager;
    }
    
    public void init(Req request, Resp response) {
        this.request = request;
        this.response = response;
    }
    
    @Override
    public void setRouteInformation(Route route, String format, String language, String routedPath) throws IllegalArgumentException {
        if (route == null)
            throw new IllegalArgumentException("Route could not be null");
        this.route = route;
        
        this.format = format;
        this.language = language;
        this.routedPath = routedPath;
    }
    
    @Override
    public Req request() {
        return request;
    }
    @Override
    public Resp response() {
        return response;
    }

    @Override
    public Request createRequest() {
        return null;
    }

    @Override
    public Route getRoute() {
        return route;
    }

    @Override
    public String getRouteLanguage() {
        String routeLang = getRouteParam("lang");
        if (routeLang != null) return routeLang;
        return language;
    }

    @Override
    public String getRouteFormat() {
        return null;
    }

    @Override
    public String getRouteParam(String name) {
        if (route == null) return null;
        return route.getPathParametersEncoded(routedPath).get(name);
    }

    @Override
    public SessionFacade getSession(boolean createIfNotExists) {
        return null;
    }

    @Override
    public void setFlash(String name, Object value) {
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
        return null;
    }

    @Override
    public String requestUrl() {
        return null;
    }

    @Override
    public String requestUri() {
        return null;
    }

    @Override
    public String queryString() {
        return null;
    }

    @Override
    public String method() {
        return null;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return null;
    }

    @Override
    public int port() {
        return 0;
    }

    @Override
    public String host() {
        return null;
    }

    @Override
    public String scheme() {
        return null;
    }

    @Override
    public String serverName() {
        return null;
    }

    @Override
    public String ipAddress() {
        return null;
    }

    @Override
    public String protocol() {
        return null;
    }

    @Override
    public String remoteHost() {
        return null;
    }

    @Override
    public String remoteAddress() {
        return null;
    }

    @Override
    public String getAcceptContentType() {
        return null;
    }

    @Override
    public String requestContentType() {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return "webapp/" + path;
    }

    @Override
    public MultiList<String> params() {
        return null;
    }

    @Override
    public String param(String name) {
        return request.param(name).orElse(getRouteParam(name));
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public <T> T getAttribute(String name, Class<T> clazz) {
        return null;
    }

    @Override
    public Map<String, String> requestHeaders() {
        return null;
    }

    @Override
    public String requestHeader(String name) {
        return null;
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
    public Cookie getCookie(String cookieName) {
        return null;
    }

    @Override
    public boolean hasCookie(String cookieName) {
        return false;
    }

    @Override
    public List<Cookie> getCookies() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
    }

    @Override
    public boolean isRequestMultiPart() {
        return false;
    }

    @Override
    public Optional<List<FormItem>> parseRequestMultiPartItems(String encoding) {
        return null;
    }

    @Override
    public String getResponseEncoding() {
        return response.characterEncoding().map(ct -> ct.name()).orElse(null);
    }

    @Override
    public void setEncoding(String encoding) {
    }

    @Override
    public void responseLocale(Locale locale) {
    }

    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public void addResponseHeader(String name, String value) {
    }

    @Override
    public Collection<String> responseHeaderNames() {
        return null;
    }

    @Override
    public PrintWriter responseWriter() throws IOException {
        return null;
    }

    @Override
    public OutputStream responseOutputStream() throws IOException {
        return null;
    }

    @Override
    public ResponseStream finalizeResponse(Response controllerResponse) {
        return finalizeResponse(controllerResponse, true);
    }

    @Override
    public ResponseStream finalizeResponse(Response controllerResponse, boolean handleFlash) {
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
