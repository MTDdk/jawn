package net.javapla.jawn.security.interfaces;

import net.javapla.jawn.core.http.Context;

public interface JSubjectContext extends ContextSource {

    void setContext(Context context);
}
