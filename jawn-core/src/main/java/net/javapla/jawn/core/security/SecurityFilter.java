package net.javapla.jawn.core.security;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.ResponseBuilder;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.spi.Filter;
import net.javapla.jawn.core.spi.FilterChain;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

public class SecurityFilter implements Filter {
    
    public SecurityFilter(String role) {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        
        // create the security manager
        //1.
        Factory<SecurityManager> factory = new JawnSecurityManagerFactory();//new IniSecurityManagerFactory("classpath:shiro.ini");
        //2.
        SecurityManager securityManager = factory.getInstance();
        //3.
        SecurityUtils.setSecurityManager(securityManager);
    }
    
    @Override
    public Response before(FilterChain chain, Context context) {
        String username = context.getParameter("username");
        String password = context.getParameter("password");
        String remember = context.getParameter("rememberMe"); //TODO handle cookie independently with framework
        System.out.println(MessageFormat.format("username {0} + pass {1} + remember {2}", username, password, remember));

        
        // get the current user
        Subject subject = context.createSession().get("user", Subject.class);//SecurityUtils.getSubject();
        if (subject == null) {
            subject = (new Subject.Builder()).buildSubject();
            //ThreadContext.bind(subject);
            context.createSession().put("user", subject);
        }
        
        
        if (!subject.isAuthenticated()) {
            System.out.println("not AUTH");
    //        boolean equal = isPasswordEqual(username, password);
    //        if (equal) {
                UsernamePasswordToken token = new UsernamePasswordToken(username, password);
                token.setRememberMe(true/*context.getParameter("rememberMe") != null*/);
                try {
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
            context.finalizeResponse(ResponseBuilder.noBody(401));// README could actually throw an exception instead
            return null;
        }
        System.out.println(subject.getPrincipal() + " is AUTH " + subject.hasRole("admin"));
        subject.checkRole("admin");
        return chain.before(context);
    }
    
    private Connection createConnection() {
        Connection conn;
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/test", "", "");
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private boolean isPasswordEqual(String username, String password) {
        try (Connection conn = createConnection()) {
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
}
