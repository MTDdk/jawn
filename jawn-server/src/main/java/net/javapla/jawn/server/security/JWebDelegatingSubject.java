package net.javapla.jawn.server.security;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.server.security.interfaces.JawnSubject;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.support.DelegatingSubject;

public class JWebDelegatingSubject extends DelegatingSubject implements JawnSubject {

    private Context context;


//    public JWebDelegatingSubject(PrincipalCollection principals, boolean authenticated, String host, Session session, boolean sessionCreationEnabled,
//            SecurityManager securityManager) {
//        super(principals, authenticated, host, session, sessionCreationEnabled, securityManager);
//    }

    
    public JWebDelegatingSubject(PrincipalCollection principals, boolean authenticated, String host, Session session, boolean sessionCreationEnabled,
            SecurityManager securityManager, Context context) {
//        this(principals, authenticated, host, session, sessionCreationEnabled,
//                securityManager);
        super(principals, authenticated, host, session, sessionCreationEnabled, securityManager);
        this.context = context;
    }


    @Override
    public Context getContext() {
        return context;
    }

}
