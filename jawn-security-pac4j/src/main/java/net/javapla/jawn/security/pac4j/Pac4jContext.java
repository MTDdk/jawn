package net.javapla.jawn.security.pac4j;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Cookie.Builder;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.util.MultiList;

public interface Pac4jContext extends WebContext {

    Context context();
    
    static Pac4jContext create(final Context context) {
        return new Pac4jContext() {
            
            @Override
            public Context context() {
                return context;
            }
            
            @Override
            public boolean isSecure() {
                return context.req().isSecure();
            }
            
            @Override
            public SessionStore<Pac4jContext> getSessionStore() {
                return new Pac4jSessionStore();
            }
            
            @Override
            public int getServerPort() {
                return context.serverPort();
            }
            
            @Override
            public String getServerName() {
                return context.serverHost();
            }
            
            @Override
            public String getScheme() {
                return context.req().scheme();
            }
            
            @Override
            public String getRemoteAddr() {
                return context.req().remoteAddress();
            }
            
            @Override
            public String getPath() {
                return context.req().path();
            }
            
            @Override
            public String getFullRequestURL() {
                StringBuilder bob = new StringBuilder();
                String scheme = getScheme();
                bob.append(scheme).append("://").append(context.serverHost());
                
                int port = context.serverPort();
                if (!(("http".equals(scheme) && port == 80)
                        || ("https".equals(scheme) && port == 443))) {
                    bob.append(":").append(port);
                }
                
                String contextPath = context.req().context();
                if (contextPath != null && contextPath.length() > 0) {
                    bob.append(contextPath);
                }
                
                bob.append(context.req().fullPath());
                
                return bob.toString();
            }
            
            @Override
            public String getRequestMethod() {
                return context.req().httpMethod().name();
            }

            @Override
            public Optional<String> getRequestHeader(String name) {
                return context.req().header(name).asOptional();
            }

            @Override
            public Collection<Cookie> getRequestCookies() {
                return context.req().cookies().values().stream().map(c -> new Cookie(c.name(), c.value())).collect(Collectors.toList());
            }

            @Override
            public Map<String, String[]> getRequestParameters() {
                LinkedHashMap<String, String[]> params = new LinkedHashMap<>();
                
                Map<String, String> path = context.req().pathParams();
                path.entrySet().forEach(e -> params.put(e.getKey(), new String[] {e.getValue()}));
                
                MultiList<String> query = context.req().queryParams();
                query.mapAndConsume((values) -> values.toArray(String[]::new), params::put);
                
                MultiList<FormItem> form = context.req().formData();
                form.mapAndConsume((values) -> values.stream().filter(item -> item.value().isPresent()).map(item -> item.value().get()).toArray(String[]::new), params::put);
                
                return params;
            }

            @Override
            public Optional<String> getRequestParameter(String name) {
                return context.param(name).asOptional();
            }

            @Override
            public Optional<Object> getRequestAttribute(String name) {
                return context.attribute(name);
            }

            @Override
            public void setResponseHeader(String name, String value) {
                context.resp().header(name, value);
            }

            @Override
            public void setResponseContentType(String contentType) {
                context.resp().contentType(contentType);
            }

            @Override
            public void setRequestAttribute(String name, Object value) {
                context.attribute(name, value);
            }

            @Override
            public void addResponseCookie(Cookie cookie) {
                Builder builder = net.javapla.jawn.core.Cookie
                        .builder(cookie.getName(), cookie.getValue());
                
                Optional.ofNullable(cookie.getDomain()).ifPresent(builder::domain);
                Optional.ofNullable(cookie.getPath()).ifPresent(builder::path);
                
                builder.httpOnly(cookie.isHttpOnly());
                builder.secure(cookie.isSecure());
                builder.maxAge(cookie.getMaxAge());
                
                // SameSite
                
                context.resp().cookie(builder.build());
            }
        };
    }
}
