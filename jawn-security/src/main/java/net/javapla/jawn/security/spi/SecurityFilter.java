package net.javapla.jawn.security.spi;

import net.javapla.jawn.core.spi.Filter;

public interface SecurityFilter extends Filter {

    SecurityFilter onRole(String role);
    SecurityFilter redirectWhenNotLoggedIn(String toUrl);
    SecurityFilter redirectWhenNotAuth(String toUrl);
    SecurityFilter logoutUrl(String url);
}
