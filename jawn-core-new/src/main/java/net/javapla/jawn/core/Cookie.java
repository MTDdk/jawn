package net.javapla.jawn.core;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * We need an internal Cookie representation, as this will make it agnostic to
 * implementation specifics such as a Servlet Cookie.
 * 
 * @author MTD
 */
public class Cookie {
    
    static final DateTimeFormatter fmt = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneId.of("GMT"));

    private static final String __COOKIE_DELIM = "\",;\\ \t";

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
    public static final int HALF_YEAR = ONE_YEAR >> 1;
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
        StringBuilder sb = new StringBuilder();

        // name = value
        appender.accept(sb, name);
        sb.append("=");
        appender.accept(sb, value);

        sb.append(";Version=");
        sb.append(version);

        // Path
        if (path != null) {
            sb.append(";Path=");
            appender.accept(sb, path);
        }

        // Domain
        if (domain != null) {
            sb.append(";Domain=");
            appender.accept(sb, domain);
        }
        
        // Secure
        if (secure) {
          sb.append(";Secure");
        }

        // HttpOnly
        if (httpOnly) {
          sb.append(";HttpOnly");
        }

        // Max-Age
        if (maxAge >= 0) {
          sb.append(";Max-Age=").append(maxAge);

          Instant instant = Instant
              .ofEpochMilli(maxAge > 0 ? System.currentTimeMillis() + maxAge * 1000L : 0);
          sb.append(";Expires=").append(fmt.format(instant));
        }

        // Comment
        if (comment != null) {
            sb.append(";Comment=");
            appender.accept(sb, comment);
        }

        return sb.toString();
    }
    
    static final Predicate<String> needQuote = (str) -> {
        if (str.length() > 1 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (__COOKIE_DELIM.indexOf(c) >= 0) {
                return true;
            }
            if (c < 0x20 || c >= 0x7f) {
                throw new IllegalArgumentException("Illegal character found at: [" + i + "]");
            }
        }
        return false;
    };
    
    static final BiConsumer<StringBuilder,String> appender = (sb,str) -> {
        if (needQuote.test(str)) {
            sb.append('"');
            for (int i = 0; i < str.length(); ++i) {
                char c = str.charAt(i);
                if (c == '"' || c == '\\') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            sb.append('"');
        } else {
            sb.append(str);
        }
    };
    
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
        
        private int version = 1;
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
        
        public Builder secure() {
            this.secure = true;
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
