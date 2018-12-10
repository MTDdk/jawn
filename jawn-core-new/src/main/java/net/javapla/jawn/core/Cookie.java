package net.javapla.jawn.core;

import java.util.Date;

/**
 * We need an internal Cookie representation, as this will make it agnostic to
 * implementation specifics such as a Servlet Cookie.
 * 
 * @author MTD
 */
public class Cookie {

    /**
     * The number of seconds in one day (= 60 * 60 * 24).
     */
    public static final int ONE_DAY = 60 * 60 * 24;
    /**
     * The number of seconds in one year (= 60 * 60 * 24 * 365).
     */
    public static final int ONE_YEAR = ONE_DAY * 365;
    /**
     * The number of seconds in half a year (= 60 * 60 * 24 * 365 / 2).
     */
    public static final int HALF_YEAR = ONE_YEAR / 2;
    /**
     * Beginning of time
     */
    public static final Date EPOCH = new Date(0L);

    private final String name;
    private final String value;
    private final String comment;
    private final int maxAge;
    private final String domain;
    private final String path;
    private final boolean secure;
    private final boolean httpOnly;
    private final int version;
    /*private final Date expires;*/

    public Cookie(final Cookie.Builder bob) {
        this.name = bob.name;
        this.value = bob.value;
        this.comment = bob.comment;
        this.maxAge = bob.maxAge;
        this.domain = bob.domain;
        this.path = bob.path;
        this.secure = bob.secure;
        this.httpOnly = bob.httpOnly;
        this.version = bob.version;
        /*this.expires = expires;*/
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String domain() {
        return domain;
    }

    public String path() {
        return path;
    }

    public String comment() {
        return comment;
    }

    public int getVersion() {
        return version;
    }
    
    /*public Date getExpires() {
        return expires;
    }*/
    
    /**
     * Tells if a cookie HTTP only or not.
     *
     * This will only work with Servlet 3
     */
    public boolean httpOnly() {
        return httpOnly;
    }

    public boolean secure() {
        return secure;
    }

    public int maxAge() {
        return maxAge;
    }

    public int version() {
        return version;
    }
    
    /*public Date getExpires() {
        return expires;
    }*/

    @Override
    public Cookie clone() {
        return new Builder(this).build();
    }

    @Override
    public String toString() {
        return "Cookie {"
                +   "name='" + name + '\''
                + ", value='" + value + '\''
                + ", domain='" + domain + '\''
                + ", path='" + path + '\''
                + ", comment='" + comment + '\''
                + ", secure=" + secure + '\''
                + ", maxAge=" + maxAge + '\''
                + ", version=" + version + '}'
                ;
    }
    
    public static class Builder {
        private final String name;
        private String value;
        private String domain;
        private String path = "/";
        private String comment;
        private boolean httpOnly = false;
        
        /** True if session cookie is only transmitted via HTTPS */
        private boolean secure = false;
        
        /** Default is -1, which indicates that the cookie will persist until browser shutdown */
        private int maxAge = -1;
        
        private int version;
        /*private Date expires;*/

        public Builder(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Builder(final Cookie cookie) {
            name = cookie.name;
            value = cookie.value;
            comment = cookie.comment;
            maxAge = cookie.maxAge;
            domain = cookie.domain;
            path = cookie.path;
            secure = cookie.secure;
            httpOnly = cookie.httpOnly;
            version = cookie.version;
            /*expires = cookie.expires;*/
        }
        
        public Cookie build() {
            return new Cookie(this);
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }
        
        public Builder maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }
        
        public Builder version(final int version) {
            this.version = version;
            return this;
        }

        /**
         * Sets this cookie to be HTTP only.
         *
         * This will only work with Servlet 3
         */
        public Builder httpOnly() {
            httpOnly = true;
            return this;
        }

        /**
         * Sets this cookie to be Http only or not
         */
        public Builder httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }
    }
}
