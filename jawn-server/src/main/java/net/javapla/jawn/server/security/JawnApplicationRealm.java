package net.javapla.jawn.server.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class JawnApplicationRealm extends AuthorizingRealm {
    
    /**
     * The default query used to retrieve account data for the user.
     */
    protected static final String DEFAULT_AUTHENTICATION_QUERY = "SELECT user_pass FROM JAWN_USERS where user_login = ?";
    /**
     * The default query used to retrieve the roles that apply to a user.
     */
    //TODO create table in DB
    protected static final String DEFAULT_USER_ROLES_QUERY = "select role_name from user_roles where username = ?";

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return null;
    }
    
    
}
