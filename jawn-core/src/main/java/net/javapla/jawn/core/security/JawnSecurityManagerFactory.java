package net.javapla.jawn.core.security;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.util.AbstractFactory;

/**
 * Factory for a {@link SecurityManager}.
 * 
 * Every call to {@link #getInstance()} results in a SecurityManager 
 * that is a singleton for this factory instance.
 * 
 * @author MTD
 *
 */
class JawnSecurityManagerFactory extends AbstractFactory<SecurityManager> {

    public JawnSecurityManagerFactory() {
        super();
    }
    
    @Override
    protected SecurityManager createInstance() {
        return createSecurityManager();
    }
    
    private SecurityManager createSecurityManager() {
        
        // create security manager
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        
        // create Realms
        Collection<Realm> realms = new ArrayList<>();
        realms.add(createSimpleRealm());
        
        // add realms to security manager
        if (!realms.isEmpty())
            applyRealmsToSecurityManager(realms, securityManager);
        
        return securityManager;
    }
    
    private void assertRealmSecurityManager(SecurityManager securityManager) {
        if (securityManager == null) {
            throw new NullPointerException("securityManager instance cannot be null");
        }
        if (!(securityManager instanceof RealmSecurityManager)) {
            String msg = "securityManager instance is not a " + RealmSecurityManager.class.getName() +
                    " instance.  This is required to access or configure realms on the instance.";
            throw new ConfigurationException(msg);
        }
    }

    protected void applyRealmsToSecurityManager(Collection<Realm> realms, SecurityManager securityManager) {
        assertRealmSecurityManager(securityManager);
        ((RealmSecurityManager) securityManager).setRealms(realms);
    }
    
    private SimpleAccountRealm createSimpleRealm() {
        SimpleAccountRealm realm = new SimpleAccountRealm();
        
        realm.addRole("admin");
        realm.addAccount("henning", "henning", "admin");
        
        return realm;
    }

}
