package net.javapla.jawn.core.security;

import org.apache.shiro.mgt.DefaultSecurityManager;

class JawnSecurityManager extends DefaultSecurityManager {

    public JawnSecurityManager() {
    }
    
    JawnApplicationRealm getRealm() {
        return null;
    }
}
