package net.javapla.jawn.core;

public interface SessionToken {
    
    /**
     * Default cookie for cookie based session stores.
     * Uses <code>jawn.session</code> as name. It never expires, use the root, only for HTTP.
     */
    Cookie SESSION_COOKIE = Cookie.builder("jawn.session", null)
        .maxAge(-1)
        .httpOnly(true)
        .path("/")
        .build();

    /**
     * Find session ID.
     *
     * @param ctx Web context.
     * @return Session ID or <code>null</code>.
     */
    String findToken(Context ctx);

    /**
     * Save session ID in the web context.
     *
     * @param ctx Web context.
     * @param token Token/data to save.
     */
    void saveToken(Context ctx, String token);

    /**
     * Delete session ID in the web context.
     *
     * @param ctx Web context.
     * @param token Token/data to delete.
     */
    void deleteToken(Context ctx, String token);
    
    /**
     * Generate a new token. This implementation produces an url encoder ID using a secure random
     * of {@link Crypto.SecretGenerator#DEFAULT_SIZE}.
     *
     * @return A new token.
     */
    default String generateId() {
        return Crypto.SecretGenerator.generateAndEncode();
    }
    
    
    
    /**
     * Create a cookie-based Session ID. This strategy:
     *
     * - find a token from a request cookie.
     * - on save, set a response cookie on new sessions or when cookie has a max-age value.
     * - on destroy, expire the cookie.
     *
     * @param cookie Cookie to use.
     * @return Session Token.
     */
    static SessionToken cookieToken(Cookie cookie) {
        return new CookieToken(cookie);
    }
    
    /**
     * Create a header-based Session Token. This strategy:
     *
     * - find a token from a request header.
     * - on save, send the header back as response header.
     * - on session destroy. don't send response header back.
     *
     * @param name Header name.
     * @return Session Token.
     */
    static SessionToken headerToken(String name) {
        return new HeaderToken(name);
    }
    
    
    
    class CookieToken implements SessionToken {
        
        private final Cookie cookie;
        
        public CookieToken(Cookie c) {
            this.cookie = c;
        }

        @Override
        public String findToken(Context ctx) {
            Cookie c = ctx.req().cookies().get(cookie.name());
            if (c == null) return null;
            return c.value();
        }

        @Override
        public void saveToken(Context ctx, String token) {
            ctx.resp().cookie(Cookie.builder(cookie).value(token).build());
        }

        @Override
        public void deleteToken(Context ctx, String token) {
            // Is setting the value even necessary?
            // Perhaps it is here and not in a SecurerSessionToken
            ctx.resp().cookie(Cookie.builder(cookie)/*.value(token)*/.maxAge(0).build());
        }
        
    }
    
    class HeaderToken implements SessionToken {
        
        private final String name;
        
        public HeaderToken(String n) {
            this.name = n;
        }

        @Override
        public String findToken(Context ctx) {
            return ctx.req().header(name).value();
        }

        @Override
        public void saveToken(Context ctx, String token) {
            ctx.resp().header(name, token);
        }

        @Override
        public void deleteToken(Context ctx, String token) {
            ctx.resp().removeHeader(name);
        }
    }
}
