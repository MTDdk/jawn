package net.javapla.jawn.server.security;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.SessionFacade;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SessionManager;

/**
 * Totally and (almost) shamelessly a re-implementation of Shiros own ServletContainerSessionManager
 * 
 * @author MTD
 */
public class JawnSecuritySessionManager implements SessionManager {


    public JawnSecuritySessionManager() {
    }
    
    @Override
    public Session start(SessionContext sessionContext) {
        if (! (sessionContext instanceof JWebSessionContext)) {
            String msg = "SessionContext must be an HTTP compatible implementation.";
            throw new IllegalArgumentException(msg);
        }
        
        Context context = ((JWebSessionContext) sessionContext).getContext();
            
        return createSession(context.getSession(true), context.remoteHost());
    }

    @Override
    public Session getSession(SessionKey key) throws SessionException {
        // The SessionKey can be used to look up sessions
        // These sessions might be saved somehow, e.g. in a database,
        // but in a web environment they are probably better of being
        // maintained by the servlet container
        
        JWebSessionKey k = (JWebSessionKey) key;
        
        SessionFacade session = k.getContext().getSession(false);
        if (session != null)
            return createSession(session, k.getContext().remoteHost());
        return null;
    }
    
    protected JawnSecuritySession createSession(SessionFacade session, String host) {
        return new JawnSecuritySession(session, host);
    }

}
