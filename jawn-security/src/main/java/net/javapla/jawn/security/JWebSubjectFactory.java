package net.javapla.jawn.security;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.security.interfaces.ContextSource;

import org.apache.shiro.mgt.DefaultSubjectFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;

public class JWebSubjectFactory extends DefaultSubjectFactory {

    public JWebSubjectFactory() {
        super();
    }
    
    @Override
    public Subject createSubject(SubjectContext subjectContext) {
        if (!(subjectContext instanceof JWebSubjectContext))
            return super.createSubject(subjectContext);
        
        JWebSubjectContext c = (JWebSubjectContext)subjectContext;
        SecurityManager securityManager = c.resolveSecurityManager();
        Session session = c.resolveSession();
        boolean sessionCreationEnabled = c.isSessionCreationEnabled();
        PrincipalCollection principals = c.resolvePrincipals();
        boolean authenticated = c.resolveAuthenticated();
        String host = c.resolveHost();
        
        Context context = c.getContext();
        if (context == null) {
            Subject subject = c.getSubject();
            if (subject instanceof ContextSource)
                context = ((ContextSource) subject).getContext();
        }
        return new JWebDelegatingSubject(principals, authenticated, host, session, sessionCreationEnabled, securityManager, context);
    }
}
