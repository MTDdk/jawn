package net.javapla.jawn.server.security;

import java.io.Serializable;

import org.apache.shiro.session.mgt.DefaultSessionKey;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.server.security.interfaces.ContextSource;

public class JWebSessionKey extends DefaultSessionKey implements ContextSource {

    private static final long serialVersionUID = -7218998656269475557L;
    
    protected final Context context;

    public JWebSessionKey(Context context) {
        if (context == null) {
            throw new NullPointerException("request argument cannot be null.");
        }
        this.context = context;
    }
    public JWebSessionKey(Serializable sessionId, Context context) {
        this(context);
        setSessionId(sessionId);
    }
    
    @Override
    public Context getContext() {
        return context;
    }

}
