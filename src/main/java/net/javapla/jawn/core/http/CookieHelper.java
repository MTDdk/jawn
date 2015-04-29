package net.javapla.jawn.core.http;

public class CookieHelper {

    public static Cookie fromServletCookie(javax.servlet.http.Cookie servletCookie){
        Cookie cookie = new Cookie(servletCookie.getName(), servletCookie.getValue());
        cookie.setMaxAge(servletCookie.getMaxAge());
        cookie.setDomain(servletCookie.getDomain());
        cookie.setPath(servletCookie.getPath());
        cookie.setSecure(servletCookie.getSecure());
        cookie.setVersion(servletCookie.getVersion());
        cookie.setHttpOnly(isHttpOnlyReflect(servletCookie));
        return cookie;
    }

    public static javax.servlet.http.Cookie toServletCookie(net.javapla.jawn.core.http.Cookie cookie){
        javax.servlet.http.Cookie servletCookie = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
        servletCookie.setMaxAge(cookie.getMaxAge());
        if (cookie.getDomain() != null)
                servletCookie.setDomain(cookie.getDomain());
        servletCookie.setPath(cookie.getPath());
        servletCookie.setSecure(cookie.isSecure());
        servletCookie.setVersion(cookie.getVersion());
        setHttpOnlyReflect(cookie, servletCookie);
        return servletCookie;
    }

    //Need to call this by reflection for backwards compatibility with Servlet 2.5
    static boolean isHttpOnlyReflect(javax.servlet.http.Cookie servletCookie){
        try {
            return (Boolean)servletCookie.getClass().getMethod("isHttpOnly").invoke(servletCookie);
        } catch (Exception e) {
//            Cookie.logger.warn("You are trying to get HttpOnly from a cookie, but it appears you are running on Servlet version before 3.0. Returning false.. which can be false!");
            return false; //return default. Should we be throwing exception here?
        }
    }

    //Need to call this by reflection for backwards compatibility with Servlet 2.5
    static void setHttpOnlyReflect(net.javapla.jawn.core.http.Cookie awCookie, javax.servlet.http.Cookie servletCookie){
        try {
            servletCookie.getClass().getMethod("setHttpOnly", boolean.class).invoke(servletCookie, awCookie.isHttpOnly());
        } catch (Exception e) {
//            Cookie.logger.warn("You are trying to set HttpOnly on a cookie, but it appears you are running on Servlet version before 3.0.");
        }
    }

}
