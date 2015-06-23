package net.javapla.jawn.core.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.ResponseBuilder;
import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.database.DatabaseConnectionAware;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.SessionFacade;
import net.javapla.jawn.core.spi.Filter;
import net.javapla.jawn.core.spi.FilterChain;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.Subject;

class SecurityFilterImpl implements SecurityFilter, DatabaseConnectionAware {
    
    protected static final String DEFAULT_NOT_LOGGED_IN_REDIRECT = "/login";
    
    protected String role;
    protected String notLoggedInRedirect;
    protected String notAuthRedirect;

    protected DatabaseConnection db;
    
    SecurityFilterImpl(String role) {
        this(role, DEFAULT_NOT_LOGGED_IN_REDIRECT);
    }
    
    SecurityFilterImpl(String role, String redirectWhenNotAuth) {
        this.role = role;
        this.notLoggedInRedirect = redirectWhenNotAuth;
    }
    
    @Override
    public Response before(FilterChain chain, Context context) {
        String username = context.getParameter("username");
        String password = context.getParameter("password");
        String remember = context.getParameter("rememberMe"); //TODO handle cookie independently with framework
        System.out.println(MessageFormat.format("username {0} + pass {1} + remember {2}", username, password, remember));

        
        // get the current user
        // if not already existing, create and save user
        SessionFacade session = context.createSession();
        Subject subject = session.get("user", Subject.class);//SecurityUtils.getSubject();
        if (subject == null) {
            subject = (new Subject.Builder()).buildSubject();
            //ThreadContext.bind(subject);
            session.put("user", subject);
        }
        
        
        if (!subject.isAuthenticated()) {
            System.out.println("not AUTH");
            //        boolean equal = isPasswordEqual(username, password);
            //        if (equal) {
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            token.setRememberMe(context.getParameter("rememberMe") != null);
            try {
                SecurityUtils.getSecurityManager().login(subject, token);
                subject.login(token);
                return chain.before(context);
            } catch ( UnknownAccountException uae ) {
                //username wasn't in the system, show them an error message?
                uae.printStackTrace();
            } catch ( IncorrectCredentialsException ice ) {
                //password didn't match, try again?
                ice.printStackTrace();
            } catch ( LockedAccountException lae ) {
                //account for that username is locked - can't login.  Show them a message?
                lae.printStackTrace();
            } catch ( AuthenticationException ae ) {
                //unexpected condition - error?
                ae.printStackTrace();
            }
            //        }
            //context.finalizeResponse(ResponseBuilder.noBody(401));// README could actually throw an exception instead
            context.finalizeResponse(ResponseBuilder.redirect(notLoggedInRedirect));
            return null;
        }
        System.out.println(subject.getPrincipal() + " is AUTH ("+role+") " + subject.hasRole(role));
        subject.checkRole(role);
        return chain.before(context);
    }
    
    private boolean isPasswordEqual(String username, String password) {
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT user_PASS  FROM JAWN_USERS where user_LOGIN = ?")) {
                statement.setString(1, username);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.first()) {
                        String pass = rs.getString("user_PASS");
                        return pass.equals(password);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void after(FilterChain chain, Context context) {chain.after(context);}

    @Override
    public void onException(FilterChain chain, Exception e) {chain.onException(e);}

    @Override
    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        SecurityManager securityManager = SecurityUtils.getSecurityManager();
        if (securityManager instanceof RealmSecurityManager) {
            Collection<Realm> realms = ((RealmSecurityManager) securityManager).getRealms();
            for (Realm realm : realms) {
                if (realm instanceof JdbcRealm) {
                    ((JdbcRealm) realm).setDataSource(databaseConnection);
                }
            }
        }
            
        this.db = databaseConnection;
    }

    @Override
    public void onRole(String role) {
        this.role = role;
    }

    @Override
    public void redirectWhenNotLoggedIn(String toUrl) {
        notLoggedInRedirect = toUrl;
    }

    @Override
    public void redirectWhenNotAuth(String toUrl) {
        notAuthRedirect = toUrl;
    }
}
