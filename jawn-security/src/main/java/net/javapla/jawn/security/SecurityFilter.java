package net.javapla.jawn.security;

import net.javapla.jawn.core.spi.Filter;

public interface SecurityFilter extends Filter {

    void onRole(String role);
    void redirectWhenNotLoggedIn(String toUrl);
    void redirectWhenNotAuth(String toUrl);
    void redirectWhenLogout(String toUrl);
    
    public static class Builder {
        private final SecurityFilterImpl filter;
        public Builder() {
            filter = new SecurityFilterImpl();
        }
        
        public Builder onRole(String role) {
            filter.onRole(role);
            return this;
        }
        public Builder redirectWhenNotLoggedIn(String toUrl) {
            filter.redirectWhenNotLoggedIn(toUrl);
            return this;
        }
        public Builder redirectWhenNotAuth(String toUrl) {
            filter.redirectWhenNotAuth(toUrl);
            return this;
        }
        public Builder redirectWhenLogout(String toUrl) {
            filter.redirectWhenLogout(toUrl);
            return this;
        }
        
        public Filter build() { return filter; }
    }
}
