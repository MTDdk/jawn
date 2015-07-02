package net.javapla.jawn.server.security;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.Cookie;
import net.javapla.jawn.server.security.interfaces.ContextSource;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.AbstractRememberMeManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.subject.WebSubject;
import org.apache.shiro.web.subject.WebSubjectContext;

/**
 * A reimplementation of Shiro's CookieRememberMeManager
 * 
 * @author MTD
 */
public class JawnRememberMeManager extends AbstractRememberMeManager {

    /**
     * The default name of the underlying rememberMe cookie which is {@code jawnRememberMe}.
     */
    public static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "jawnRememberMe";
    
    public static final String IDENTITY_REMOVED_KEY = JawnRememberMeManager.class.getName() + "_IDENTITY_REMOVED_KEY";
    
    protected Cookie cookie;
    
    /**
     * Constructs a new {@code CookieRememberMeManager} with a default {@code rememberMe} cookie template.
     */
    public JawnRememberMeManager() {
        Cookie cookie = new Cookie(DEFAULT_REMEMBER_ME_COOKIE_NAME);
        cookie.setHttpOnly();
        cookie.setMaxAge(Cookie.ONE_DAY);
        
        this.cookie = cookie;
    }
    
    /**
     * Returns the cookie 'template' that will be used to set all attributes of outgoing rememberMe cookies created by
     * this {@code RememberMeManager}.  Outgoing cookies will match this one except for the
     * {@link Cookie#getValue() value} attribute, which is necessarily set dynamically at runtime.
     * <p/>
     * Please see the class-level JavaDoc for the default cookie's attribute values.
     *
     * @return the cookie 'template' that will be used to set all attributes of outgoing rememberMe cookies created by
     *         this {@code RememberMeManager}.
     */
    public Cookie getCookie() {
        return cookie;
    }

    /**
     * Sets the cookie 'template' that will be used to set all attributes of outgoing rememberMe cookies created by
     * this {@code RememberMeManager}.  Outgoing cookies will match this one except for the
     * {@link Cookie#getValue() value} attribute, which is necessarily set dynamically at runtime.
     * <p/>
     * Please see the class-level JavaDoc for the default cookie's attribute values.
     *
     * @param cookie the cookie 'template' that will be used to set all attributes of outgoing rememberMe cookies created
     *               by this {@code RememberMeManager}.
     */
    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }
    
    @Override
    public void forgetIdentity(SubjectContext subjectContext) {
    }

    @Override
    protected void forgetIdentity(Subject subject) {
    }

    /**
     * Base64-encodes the specified serialized byte array and sets that base64-encoded String as the cookie value.
     * <p/>
     * The {@code subject} instance is expected to be a {@link WebSubject} instance with an HTTP Request/Response pair
     * so an HTTP cookie can be set on the outgoing response.  If it is not a {@code WebSubject} or that
     * {@code WebSubject} does not have an HTTP Request/Response pair, this implementation does nothing.
     *
     * @param subject    the Subject for which the identity is being serialized.
     * @param serialized the serialized bytes to be persisted.
     */
    @Override
    protected void rememberSerializedIdentity(Subject subject, byte[] serialized) {
        if (! (subject instanceof ContextSource)) {
//            if (log.isDebugEnabled()) {
//                String msg = "Subject argument is not an HTTP-aware instance.  This is required to obtain a servlet " +
//                        "request and response in order to set the rememberMe cookie. Returning immediately and " +
//                        "ignoring rememberMe operation.";
//                log.debug(msg);
//            }
            return;
        }
        
        Context context = ((ContextSource) subject).getContext();
        
        //base 64 encode it and store as a cookie
        String base64 = Base64.encodeToString(serialized); // could be java.util.Base64
        
        Cookie cookie = getCookie().clone();
        cookie.setValue(base64);
        context.addCookie(cookie);// save the cookie
    }

    /**
     * Returns a previously serialized identity byte array or {@code null} if the byte array could not be acquired.
     * This implementation retrieves an HTTP cookie, Base64-decodes the cookie value, and returns the resulting byte
     * array.
     * <p>
     * The {@code SubjectContext} instance is expected to be a {@link WebSubjectContext} instance with an HTTP
     * Request/Response pair so an HTTP cookie can be retrieved from the incoming request.  If it is not a
     * {@code WebSubjectContext} or that {@code WebSubjectContext} does not have an HTTP Request/Response pair, this
     * implementation returns {@code null}.
     *
     * @param subjectContext the contextual data, usually provided by a {@link Subject.Builder} implementation, that
     *                       is being used to construct a {@link Subject} instance.  To be used to assist with data
     *                       lookup.
     * @return a previously serialized identity byte array or {@code null} if the byte array could not be acquired.
     */
    @Override
    protected byte[] getRememberedSerializedIdentity(SubjectContext subjectContext) {
        // no need to check for HTTP-aware context - jawn is always in a web environment
        if (! (subjectContext instanceof ContextSource)) {
//            if (log.isDebugEnabled()) {
//                String msg = "SubjectContext argument is not an HTTP-aware instance.  This is required to obtain a " +
//                        "servlet request and response in order to retrieve the rememberMe cookie. Returning " +
//                        "immediately and ignoring rememberMe operation.";
//                log.debug(msg);
//            }
            return null;
        }
            
        Context context = ((ContextSource) subjectContext).getContext();
        
        if (context == null || isIdentityRemoved(context))
            return null;
        
        String base64 = readCookieValue(context);
        // Browsers do not always remove cookies immediately (SHIRO-183)
        // ignore cookies that are scheduled for removal
        // TODO not yet implemented
        //if (Cookie.DELETED_COOKIE_VALUE.equals(base64)) return null;
        
        if (base64 != null) {
            base64 = ensurePadding(base64);
//            if (log.isTraceEnabled()) {
//                log.trace("Acquired Base64 encoded identity [" + base64 + "]");
//            }
            byte[] decoded = Base64.decode(base64);
//            if (log.isTraceEnabled()) {
//                log.trace("Base64 decoded byte array length: " + (decoded != null ? decoded.length : 0) + " bytes.");
//            }
            return decoded;
        } else {
            //no cookie set - new site visitor?
            return null;
        }
    }

    private boolean isIdentityRemoved(Context context) {
        Boolean removed = context.getAttribute(IDENTITY_REMOVED_KEY,Boolean.class);
        return removed != null && removed;
    }
    
    protected String readCookieValue(Context context) {
        Cookie cookie = context.getCookie(getCookie().getName());
        String value = null;
        if (cookie != null)
            value = cookie.getValue();
        return value;
    }
    
    /**
     * Sometimes a user agent will send the rememberMe cookie value without padding,
     * most likely because {@code =} is a separator in the cookie header.
     * <p/>
     * Contributed by Luis Arias.  Thanks Luis!
     *
     * @param base64 the base64 encoded String that may need to be padded
     * @return the base64 String padded if necessary.
     */
    private String ensurePadding(String base64) {
        int length = base64.length();
        if (length % 4 != 0) {
            StringBuilder sb = new StringBuilder(base64);
            for (int i = 0; i < length % 4; ++i) {
                sb.append('=');
            }
            base64 = sb.toString();
        }
        return base64;
    }
}
