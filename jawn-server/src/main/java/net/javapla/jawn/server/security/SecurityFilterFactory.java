package net.javapla.jawn.server.security;

import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.security.SecurityFilter;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;

public class SecurityFilterFactory {
    
    // This kind of synchronization is never actually needed, as
    // this factory always will be called sequentially during framework bootstrap.
    // Might be removed in near future (famous last words)
    private static final Object mutex = new Object();  
    private static boolean INITIALISED = false;
    
    /*public static SecurityFilter filterOnRole(String role) {
        initialiseSecurityManager();
        return new SecurityFilterImpl(role);
    }
    
    public static Filter filterOnRole(String role, String redirectWhenNotAuth) {
        initialiseSecurityManager();
        return new SecurityFilterImpl(role, redirectWhenNotAuth);
    }*/
    
    public static SecurityFilter filter(DatabaseConnection conn, String role) {
        initialiseSecurityManager(conn);
        return new SecurityFilterImpl(role);
    }
    
    private static synchronized void initialiseSecurityManager(DatabaseConnection conn) {
        if (INITIALISED) return;
        synchronized(mutex) {
            if (INITIALISED) return;
            // create the security manager
            //1.
            Factory<SecurityManager> factory = new JawnSecurityManagerFactory(conn);
            //2.
            SecurityManager securityManager = factory.getInstance();
//            securityManager.getRealm().
            //3.
            SecurityUtils.setSecurityManager(securityManager);
            INITIALISED = true;
        }
    }
}
