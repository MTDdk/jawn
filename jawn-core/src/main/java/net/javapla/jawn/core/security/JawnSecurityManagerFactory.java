package net.javapla.jawn.core.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.util.AbstractFactory;

/**
 * Factory for a {@link SecurityManager}.
 * 
 * Every call to {@link #getInstance()} results in a SecurityManager 
 * that is a singleton for this factory instance.
 * 
 * @author MTD
 */
class JawnSecurityManagerFactory extends AbstractFactory<JawnSecurityManager> {

    private DataSource dataSource;

    public JawnSecurityManagerFactory() {
        super();
    }
    
    JawnSecurityManagerFactory(DataSource spec) {
        this.dataSource = spec;
    }
    
    @Override
    protected JawnSecurityManager createInstance() {
        return createSecurityManager();
    }
    
    private JawnSecurityManager createSecurityManager() {

        // create security manager
        JawnSecurityManager securityManager = new JawnSecurityManager();

        Collection<Realm> realms = readRealms();

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
    
    private Collection<Realm> readRealms() {
        // create Realms
        Collection<Realm> realms = new ArrayList<>();
        try {
            realms.add(new IniRealm("classpath:security.ini"));
        } catch (IllegalArgumentException | ConfigurationException e){
            // one failed - use default instead
            realms.add(new IniRealm("classpath:jawn_default_security.ini"));
        }
        //realms.add(createJdbcRealm());
        //realms.add(createSimpleRealm());
        
        return realms;
    }
    
    private SimpleAccountRealm createSimpleRealm() {
        SimpleAccountRealm realm = new SimpleAccountRealm();
        
        realm.addRole("admin");
        realm.addAccount("henning", "henning", "admin");
        
        return realm;
    }
    
    private JdbcRealm createJdbcRealm() {
        JdbcRealm realm = new JdbcRealm();
        realm.setDataSource(dataSource);
        realm.setAuthenticationQuery("SELECT user_pass FROM JAWN_USERS where user_login = ?");
        realm.setUserRolesQuery("select role_name from user_roles where username = ?");
        realm.setPermissionsLookupEnabled(false);
        realm.setSaltStyle(SaltStyle.NO_SALT); // README this of course needs to be implemented at some point
        return realm;
    }

}
