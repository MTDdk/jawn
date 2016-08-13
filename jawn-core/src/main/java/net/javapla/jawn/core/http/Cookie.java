package net.javapla.jawn.core.http;

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

    private final String name;
    private final String value;
    private final String comment;
    private final int maxAge;
    private final String domain;
    private final String path;
    private final boolean secure;
    private final boolean httpOnly;
    private final int version;

    public Cookie(
                String name, 
                String value, 
                String comment, 
                int maxAge, 
                String domain, 
                String path, 
                boolean secure, 
                boolean httpOnly, 
                int version) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.maxAge = maxAge;
        this.domain = domain;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.version = version;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getPath() {
        return path;
    }

    public String getComment() {
        return comment;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getVersion() {
        return version;
    }

    /**
     * Tells if a cookie HTTP only or not.
     *
     * This will only work with Servlet 3
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    @Override
    public Cookie clone() {
        return new Builder(this).build();
    }

    @Override
    public String toString() {
        return "Cookie {"
                +   "name='" + name + '\''
                + ", value='" + value + '\''
                + ", comment='" + comment + '\''
                + ", maxAge=" + maxAge + '\''
                + ", domain='" + domain + '\''
                + ", path='" + path + '\''
                + ", secure=" + secure + '\''
                + ", version=" + version + '}';
    }
    
    public static Builder builder(String name, String value) {
        return new Builder(name, value);
    }
    public static Builder builder(Cookie cookie) {
        return new Builder(cookie);
    }

    public static class Builder {
        private final String name;
        private String value;
        private String comment;
        private int maxAge = -1;
        private String domain;
        private String path = "/";
        private boolean secure;
        private boolean httpOnly;
        private int version;

        private Builder(String name, String value) {
            this.name = name;
            this.value = value;
        }

        private Builder(Cookie cookie) {
            name = cookie.name;
            value = cookie.value;
            comment = cookie.comment;
            maxAge = cookie.maxAge;
            domain = cookie.domain;
            path = cookie.path;
            secure = cookie.secure;
            httpOnly = cookie.httpOnly;
            version = cookie.version;
        }
        
        public Cookie build() {
            return new Cookie(name, value, comment, maxAge, domain, path, secure, httpOnly, version);
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }
        
        public Builder setMaxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder setVersion(int version) {
            this.version = version;
            return this;
        }

        /**
         * Sets this cookie to be HTTP only.
         *
         * This will only work with Servlet 3
         */
        public Builder setHttpOnly() {
            httpOnly = true;
            return this;
        }

        /**
         * Sets this cookie to be Http only or not
         */
        public Builder setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }
    }
}
