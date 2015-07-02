package net.javapla.jawn.server.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.javapla.jawn.core.http.SessionFacade;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.util.StringUtils;

/**
 * Re-implementation of Shiros HttpServletSession
 * 
 * @author MTD
 */
public class JawnSecuritySession implements org.apache.shiro.session.Session {
    
    private static final String HOST_SESSION_KEY = JawnSecuritySession.class.getName() + ".HOST_SESSION_KEY";
    private static final String TOUCH_OBJECT_SESSION_KEY = JawnSecuritySession.class.getName() + ".TOUCH_OBJECT_SESSION_KEY";
    
    private SessionFacade session;

    JawnSecuritySession(SessionFacade session, String host) {
        this.session = session;
        if (StringUtils.hasText(host)) {
            setHost(host);
        }
    }

    @Override
    public Serializable getId() {
        return session.getId();
    }

    @Override
    public Date getStartTimestamp() {
        return new Date(session.getCreationTime());
    }

    @Override
    public Date getLastAccessTime() {
        return new Date(session.getLastAccessedTime());
    }

    @Override
    public long getTimeout() throws InvalidSessionException {
        return session.getTimeToLive() * 1000;
    }

    @Override
    public void setTimeout(long maxIdleTimeInMillis) throws InvalidSessionException {
        try {
            int timeout = Long.valueOf(maxIdleTimeInMillis / 1000).intValue();
            session.setTimeToLive(timeout);
        } catch (Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public String getHost() {
        return (String) getAttribute(HOST_SESSION_KEY);
    }
    
    protected void setHost(String host) {
        setAttribute(HOST_SESSION_KEY, host);
    }

    @Override
    public void touch() throws InvalidSessionException {
        //just manipulate the session to update the access time:
        try {
            session.setAttribute(TOUCH_OBJECT_SESSION_KEY, TOUCH_OBJECT_SESSION_KEY);
            session.removeAttribute(TOUCH_OBJECT_SESSION_KEY);
        } catch (Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public void stop() throws InvalidSessionException {
        session.invalidate();
    }

    @Override
    public Collection<Object> getAttributeKeys() throws InvalidSessionException {
        String[] names = session.names();
        Collection<Object> keys = null;
        if (names != null) {
            keys = new ArrayList<>();
            for (String name : names) {
                keys.add(name);
            }
        }
        return keys;
    }

    @Override
    public Object getAttribute(Object key) throws InvalidSessionException {
        try {
            return session.getAttribute(assertString(key));
        } catch (Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public void setAttribute(Object key, Object value) throws InvalidSessionException {
        try {
            session.setAttribute(assertString(key), value);
        } catch (Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public Object removeAttribute(Object key) throws InvalidSessionException {
        try {
            String sKey = assertString(key);
            Object removed = session.getAttribute(sKey);
            session.removeAttribute(sKey);
            return removed;
        } catch (Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    private static String assertString(Object key) {
        if (!(key instanceof String)) {
            String msg = "HttpSession based implementations of the Shiro Session interface requires attribute keys " +
                    "to be String objects.  The HttpSession class does not support anything other than String keys.";
            throw new IllegalArgumentException(msg);
        }
        return (String) key;
    }
}
