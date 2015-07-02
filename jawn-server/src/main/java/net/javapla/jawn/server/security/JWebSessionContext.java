package net.javapla.jawn.server.security;

import java.util.Map;

import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.server.security.interfaces.JSessionContext;

import org.apache.shiro.session.mgt.DefaultSessionContext;

public class JWebSessionContext extends DefaultSessionContext implements JSessionContext {

    private static final long serialVersionUID = 6801344625150638346L;
    
    protected static final String FRAMEWORK_CONTEXT = JWebSessionContext.class.getName() + ".context";

    public JWebSessionContext() {
        super();
    }
    
    public JWebSessionContext(Map<String, Object> map) {
        super(map);
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
