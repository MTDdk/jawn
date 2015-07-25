package net.javapla.jawn.security;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.security.interfaces.ContextSource;
import net.javapla.jawn.security.interfaces.JWebSecurityManager;
import net.javapla.jawn.security.interfaces.JawnSubject;

import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

class JawnSecurityManager extends DefaultWebSecurityManager implements JWebSecurityManager {
    

    public JawnSecurityManager() {
        super();
    }
    

    @Override
    protected SubjectContext createSubjectContext() {
        return new JWebSubjectContext();
    }
    
    @Override
    protected SubjectContext copy(SubjectContext subjectContext) {
        if (subjectContext instanceof JWebSubjectContext) {
            return new JWebSubjectContext(subjectContext);
        }
        return super.copy(subjectContext);
    }
    
    @Override
    protected SessionKey getSessionKey(SubjectContext subjectContext) {
        if (subjectContext instanceof ContextSource) {
            Context context = ((ContextSource) subjectContext).getContext();
            //SessionFacade session = context.getSession(false);
            //if (session != null) return new JWebSessionKey(session.getId(), context);
            return new JWebSessionKey(subjectContext.getSessionId(), context);
        }
        return super.getSessionKey(subjectContext);
    }
    
    @Override
    protected void removeRequestIdentity(Subject subject) {
        if (subject instanceof JawnSubject) {
            ((JawnSubject) subject).getContext().setAttribute(JawnRememberMeManager.IDENTITY_REMOVED_KEY, Boolean.TRUE);
        } else {
            super.removeRequestIdentity(subject);
        }
    }
}
