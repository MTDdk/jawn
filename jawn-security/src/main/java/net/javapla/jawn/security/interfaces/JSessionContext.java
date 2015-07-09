package net.javapla.jawn.security.interfaces;

import net.javapla.jawn.core.http.Context;

import org.apache.shiro.session.mgt.SessionContext;

public interface JSessionContext extends SessionContext, ContextSource {

    void setContext(Context context);
}
