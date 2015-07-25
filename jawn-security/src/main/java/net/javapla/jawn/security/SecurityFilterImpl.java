package net.javapla.jawn.security;

import java.text.MessageFormat;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.ResponseBuilder;
import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.http.SessionFacade;
import net.javapla.jawn.core.spi.FilterChain;
import net.javapla.jawn.security.interfaces.JawnSubject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;


//TODO automatically handle /login /logout
//how would this affect routing?
class SecurityFilterImpl implements SecurityFilter {
    
    protected static final String SESSION_USER = "SecurityFilterImpl.user";
    protected static final String SESSION_REQUESTED_PATH = "SecurityFilterImpl.path";
    protected static final String DEFAULT_NOT_LOGGED_IN_REDIRECT = "/login";
    protected static final String DEFAULT_NOT_CORRECTLY_AUTH = "/login?failure=notAuthorized";
    protected static final String DEFAULT_LOG_OUT = "/logout";
    
    protected String role;
    protected String notLoggedInRedirect = DEFAULT_NOT_LOGGED_IN_REDIRECT;
    protected String notAuthRedirect = DEFAULT_NOT_CORRECTLY_AUTH;
    protected String logout = DEFAULT_LOG_OUT;

    protected DatabaseConnection db;
    
    SecurityFilterImpl() {
        
    }
    
    /*public SecurityFilterImpl(String role) {
        this(role, DEFAULT_NOT_LOGGED_IN_REDIRECT);
    }
    
    SecurityFilterImpl(String role, String redirectWhenNotAuth) {
        this.role = role;
        this.notLoggedInRedirect = redirectWhenNotAuth;
    }*/
    
    @Override
    public Response before(FilterChain chain, Context context) {
        SessionFacade session = context.getSession(false);//true?
        
        if (context.getHttpMethod() == HttpMethod.GET) {
            String path = context.path();
            if (path.equals(notLoggedInRedirect))
                return chain.before(context);
            else if (path.equals(logout)) {
//                SessionFacade session = context.getSession(false);
                if (session != null) {
                    Subject subject = session.get(SESSION_USER, Subject.class);
                    if (subject != null) {
                        subject.logout();
                        session.remove(SESSION_USER);
                    }
                }
                return chain.before(context);
            }
        }
        // is a session already active?
        // - if not -> surely an user cannot already have logged in (disregarding cookies at this time)
        // does the session have a subject associated?
        //  - if not -> an user is of course not authenticated
        // the subject exists
        // - (should we ask if authenticated, or is this auth enough?)
        // does the subject have the required role?
        // - if not -> return not authorized response
        
        String username = context.getParameter("username");
        String password = context.getParameter("password");
        String remember = context.getParameter("rememberMe"); //TODO handle cookie independently with framework
        
        System.out.println(MessageFormat.format("username {0} + pass {1} + remember {2} --  ip {3}", username, password, remember, context.remoteHost()));

        // get the current user
        // if not already existing, create and save user
        if (session == null) {
            session = context.getSession(true);
            
//            context.finalizeResponse(ResponseBuilder.redirect(notLoggedInRedirect));
//            return null;//TODO Response with login html
        }
        
        
        
        //according to AbstractShiroFilter:
        //get request + response (meaning Context)
        //create a subject
        //updateSessionLastAccessTime
        Subject sub = createSubject(context);
        updateSessionLastAccessTime(sub);
        session.put(SESSION_USER, sub);
        
        //Only trust cookies for a period of time
        //Perhaps have the date as a part of the principal (or an extra DatePrincipal)
        //When date is exceeded re-login
        //The IP<->Cookie relationship also has to be taken into consideration
        //Perhaps this also ought to be a principal (IpPrincipal)
        System.out.println("is remembered? " + sub.isRemembered());
//        System.out.println(sub.getPrincipals().getPrimaryPrincipal());
        
        Subject subject = sub;
/*        Subject subject = session.get(SESSION_USER, Subject.class);
        if (subject == null) {
            ContextImpl c = (ContextImpl)context;
            subject = new WebSubject.Builder(c.getOriginalRequest(), c.getOriginalResponse())
//                            .sessionId(session.getId())
                            .host(context.ipAddress())
                            .sessionCreationEnabled(false)
                            .buildSubject();
//            ThreadContext.bind(subject);
            session.put(SESSION_USER, subject);
//            context.finalizeResponse(ResponseBuilder.redirect(notLoggedInRedirect));
//            return null;
        }*/
        
        if (!subject.isAuthenticated()) {
            
            System.out.println("not AUTH");
            
            if (context.getHttpMethod() == HttpMethod.POST) {
                
            
                UsernamePasswordToken token = new UsernamePasswordToken(username, password);
                token.setHost(context.host());
                token.setRememberMe(context.getParameter("rememberMe") != null); //TODO rememberme needs to be implemented manually
                try {
    //                SecurityUtils.getSecurityManager().login(subject, token);
                    subject.login(token);
                    
                } catch ( UnknownAccountException | IncorrectCredentialsException ice ) {
                    //UnknownAccountException:       username wasn't in the system, show them an error message?
                    //IncorrectCredentialsException: password didn't match, try again?
                    context.setFlash("credentials", "not match");
                    return  ResponseBuilder.redirect(notLoggedInRedirect + "?credentials");//add flash telling the credentials were not correct
                } catch ( LockedAccountException lae ) {
                    //account for that username is locked - can't login.  Show them a message?
                    lae.printStackTrace();
                } catch ( AuthenticationException ae ) {
                    //unexpected condition - error?
                    ae.printStackTrace();
                }
            //context.finalizeResponse(ResponseBuilder.noBody(401));// README could actually throw an exception instead
            } else {
                //save the path for later use
                session.put(SESSION_REQUESTED_PATH, context.path());
                return ResponseBuilder.redirect(notLoggedInRedirect);
            }
        }
        
        System.out.println(subject.getPrincipal() + " is AUTH ("+role+") " + subject.hasRole(role));
        
        
        //login was successful (whether the subject is authenticated or not)
        
        if (subject.hasRole(role)) {
            String location = session.get(SESSION_REQUESTED_PATH, String.class);
            session.remove(SESSION_REQUESTED_PATH);
            if (location != null)
                return ResponseBuilder.redirect(location);
            else
                return chain.before(context);
        } else
            //if not auth
        return ResponseBuilder.redirect(notAuthRedirect);
            
    }
    

    @Override
    public void after(FilterChain chain, Context context) {chain.after(context);}

    @Override
    public void onException(FilterChain chain, Exception e) {chain.onException(e);}

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
    
    @Override
    public void redirectWhenLogout(String url) {
        logout = url;
    }
    
    protected JawnSubject createSubject(Context context) {
        return new JawnSubject.Builder(context).buildJawnSubject();
    }
    
    /**
     * Updates any 'native'  Session's last access time that might exist to the timestamp when this method is called.
     * If native sessions are not enabled (that is, standard Servlet container sessions are being used) or there is no
     * session ({@code subject.getSession(false) == null}), this method does nothing.
     * <p/>This method implementation merely calls
     * <code>Session.{@link org.apache.shiro.session.Session#touch() touch}()</code> on the session.
     *
     * @param request  incoming request - ignored, but available to subclasses that might wish to override this method
     * @param response outgoing response - ignored, but available to subclasses that might wish to override this method
     * @since 1.0
     */
    protected void updateSessionLastAccessTime(Subject subject) {
//        if (!isHttpSessions()) { //'native' sessions
            //Subject subject = SecurityUtils.getSubject();
            //Subject should never _ever_ be null, but just in case:
            if (subject != null) {
                Session session = subject.getSession(false);
                if (session != null) {
                    try {
                        session.touch();
                    } catch (Throwable t) {
//                        log.error("session.touch() method invocation has failed.  Unable to update" +
//                                "the corresponding session's last access time based on the incoming request.", t);
                    }
                }
            }
//        }
    }
}
