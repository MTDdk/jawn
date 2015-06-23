package net.javapla.jawn.core.security;

import net.javapla.jawn.core.spi.Filter;

public interface SecurityFilter extends Filter {

    void onRole(String role);
    void redirectWhenNotLoggedIn(String toUrl);
    void redirectWhenNotAuth(String toUrl);
}
