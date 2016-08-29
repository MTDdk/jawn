package net.javapla.jawn.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.Req;
import net.javapla.jawn.core.http.Request;
import net.javapla.jawn.core.http.Resp;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.http.SessionFacade;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.uploads.FormItem;
import net.javapla.jawn.core.util.MultiList;

public class ServerContext implements Context {
    
    
    public void init(Req request, Resp response) {
        
    }

    @Override
    public Request createRequest() {
        return null;
    }

    @Override
    public Route getRoute() {
        return null;
    }

    @Override
    public String getRouteLanguage() {
        return null;
    }

    @Override
    public String getRouteFormat() {
        return null;
    }

    @Override
    public String getRouteParam(String name) {
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public MultiList<String> params() {
        return null;
    }

    @Override
    public String param(String name) {
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public ResponseStream finalizeResponse(Response controllerResponse, boolean handleFlash) {
        return null;
    }

}
