package net.javapla.jawn.server.security;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.server.security.interfaces.JSubjectContext;

import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.subject.support.DefaultSubjectContext;

public class JWebSubjectContext extends DefaultSubjectContext implements JSubjectContext {

    private static final long serialVersionUID = 1836200883870482366L;
    
    protected static final String FRAMEWORK_CONTEXT = JWebSubjectContext.class.getName() + ".CONTEXT";
    
    public JWebSubjectContext() {
    }
    
    public JWebSubjectContext(SubjectContext copy) {
        super(copy);
    }
    
    @Override
    public Context getContext() {
        return getTypedValue(FRAMEWORK_CONTEXT, Context.class);
    }

    @Override
    public void setContext(Context context) {
        if (context != null) {
            put(FRAMEWORK_CONTEXT, context);
        }
    }

    
}
