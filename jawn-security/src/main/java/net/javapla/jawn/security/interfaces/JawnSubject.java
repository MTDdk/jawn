package net.javapla.jawn.security.interfaces;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.security.JWebSubjectContext;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;

public interface JawnSubject extends Subject, ContextSource {

    public static class Builder extends Subject.Builder {
        
        public Builder(Context context) {
            this(SecurityUtils.getSecurityManager(), context);
        }
        
        public Builder(SecurityManager manager, Context context) {
            super(manager);
            if (context == null) {
                throw new IllegalArgumentException("Context argument cannot be null.");
            }
            setContext(context);
            //getSubjectContext().setPrincipals();
        }
        
        @Override
        protected SubjectContext newSubjectContextInstance() {
            return new JWebSubjectContext();
        }
        
        protected Builder setContext(Context context) {
            if (context != null)
                ((JSubjectContext) getSubjectContext()).setContext(context);
            return this;
        }
        
        public JawnSubject buildJawnSubject() {
            Subject subject = super.buildSubject();
            if (!(subject instanceof JawnSubject)) {
                String msg = "Subject implementation returned from the SecurityManager was not a " +
                        JawnSubject.class.getName() + " implementation.  Please ensure a jawn-enabled SecurityManager " +
                        "has been configured and made available to this builder.";
                throw new IllegalStateException(msg);
            }
            return (JawnSubject) subject;
        }
    }
}
