package net.javapla.jawn.core;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.javapla.jawn.core.Crypto.Signer;
import net.javapla.jawn.core.util.DataCodec;

public interface SessionStore {

    /** Default session timeout in minutes. */
    Duration DEFAULT_TIMEOUT = Duration.ofMinutes(30);

    /**
     * Creates a new session. This method must:
     *
     * @param ctx
     *            Web context.
     * @return A new session.
     */
    Session newSession(Context ctx);

    /**
     * Find an existing session by ID. For existing session this method must:
     *
     * - Optionally, Retrieve/restore session creation time
     *
     * @param ctx
     *            Web context.
     * @return An existing session or <code>null</code>.
     */
    Session findSession(Context ctx);

    /**
     * Delete a session from store. This method must NOT call
     * {@link Session#destroy()}.
     *
     * @param ctx
     *            Web context.
     * @param session
     *            Current session.
     */
    void deleteSession(Context ctx, Session session);

    /**
     * Session attributes/state has changed. Every time a session attribute is
     * put or removed it,
     * this method is executed as notification callback.
     *
     * @param ctx
     *            Web context.
     * @param session
     *            Current session.
     */
    void touchSession(Context ctx, Session session);

    /**
     * Save a session. This method must save:
     *
     * - Session attributes/data
     * - Optionally set Session metadata like: creationTime, lastAccessed time,
     * etc.
     *
     * This method is call after response is send to client, so context and
     * response shouldn't be
     * modified.
     *
     * @param ctx
     *            Web context.
     * @param session
     *            Current session.
     */
    void saveSession(Context ctx, Session session);

    /**
     * Renew Session ID. This operation might or might not be implemented by a
     * Session Store.
     *
     * @param ctx
     *            Web Context.
     * @param session
     *            Session.
     */
    void renewSessionToken(Context ctx, Session session);
    
    
    
    static SessionStore memory() {
        return memory(SessionToken.SESSION_COOKIE);
    }
    
    static SessionStore memory(Duration timeout) {
        return memory(SessionToken.SESSION_COOKIE, timeout);
    }
    
    static SessionStore memory(Cookie cookie) {
        return memory(SessionToken.cookieToken(cookie));
    }
    
    static SessionStore memory(Cookie cookie, Duration timeout) {
        return memory(SessionToken.cookieToken(cookie), timeout);
    }
    
    /**
     * Creates a session store that save data in memory.
     * - Session expires after 30 minutes of inactivity.
     * - Session data is not keep after restart.
     *
     * @param token Session token.
     * @return Session store.
     */
    static SessionStore memory(final SessionToken token) {
        return memory(token, DEFAULT_TIMEOUT);
    }
    

    static SessionStore memory(final SessionToken token, final Duration to) {
        return new SessionStore() {
            final Duration timeout  = to.toMillis() > 0 ? null : to;
            final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
            /*class Data {
                //private Instant lastAccessedTime;
                private Instant creationTime;
                private Map<String, String> data;
            
                public Data(Instant creationTime, Instant lastAccessedTime, Map<String, String> data) {
                    this.creationTime = creationTime;
                    //this.lastAccessedTime = lastAccessedTime;
                    this.data = data;
                }
            
                public boolean isExpired(Duration timeout) {
                    Duration timeElapsed = Duration.between(creationTime lastAccessedTime, Instant.now());
                    return timeElapsed.compareTo(timeout) > 0;
                }
            }*/

            @Override
            public Session newSession(Context ctx) {
                String sessionId = token.generateId();
                Session session = getOrCreate(ctx, sessionId);

                //Session session = restore(ctx, sessionId, data);

                token.saveToken(ctx, sessionId);
                return session;
            }

            @Override
            public Session findSession(Context ctx) {
                purge();
                
                String sessionId = token.findToken(ctx);
                if (sessionId == null) {
                    return null;
                }
                Session session = sessions.get(sessionId);//getOrNull(sessionId);
                if (session != null) {
                    //Session session = restore(ctx, sessionId, data);
                    token.saveToken(ctx, sessionId);
                    return session;
                }
                return null;
            }
            
            @Override
            public void saveSession(Context ctx, Session session) {
                String sessionId = session.getId();
                session.updateAccess();
                sessions.put(sessionId, session);//new Data(session.getCreationTime(), Instant.now(), session.toMap()));
            }

            @Override
            public void touchSession(Context ctx, Session session) {
                saveSession(ctx, session);
                token.saveToken(ctx, session.getId());
            }

            @Override
            public void renewSessionToken(Context ctx, Session session) {
                String oldId = session.getId();
                /*Data data = sessions.remove(oldId);
                if (data != null) {
                    String newId = token.newToken();
                    session.setId(newId);

                    put(newId, data);
                }*/
                Session old = sessions.remove(oldId);
                if (old != null) {
                    String newId = token.generateId();
                    sessions.put(newId, Session.create(ctx, newId, session.created(), session.data()));
                }
                
            }

            @Override
            public void deleteSession(Context ctx, Session session) {
                String sessionId = session.getId();
                sessions.remove(sessionId);
                token.deleteToken(ctx, sessionId);
            }

            private Session getOrCreate(Context ctx, String sessionId) {
                return sessions.computeIfAbsent(sessionId, sid -> Session.create(ctx, sessionId));//new Data(Instant.now(), Instant.now(), new ConcurrentHashMap<>()));
            }

            /*private Session restore(Context ctx, String sessionId, Data data) {
                return Session.create(ctx, sessionId, data.creationTime, data.data);
            }*/
            
            /**
            * Check for expired session and delete them.
            */
           private void purge() {
               if (timeout != null) {
                   Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator();
                   while (iterator.hasNext()) {
                       Map.Entry<String, Session> entry = iterator.next();
                       Session session = entry.getValue();
                       if (session.isExpired(timeout)) {
                           iterator.remove();
                       }
                   }
               }
           }
        };
    }

    
    /**
     * Creates a session store that uses (un)signed data. Session data is signed it using
     * <code>HMAC_SHA256</code>.
     *
     * @param secret Secret token to signed data.
     * @param token Session token to use.
     * @return A browser session store.
     */
    static SessionStore signed(String secret, SessionToken token) {
        final Signer signer = Crypto.Signer.SHA256(secret);
        
        Function<String, Map<String, String>> decoder = value -> {
            value = new String(
                Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8)), 
                StandardCharsets.UTF_8);
            
            int sep = value.indexOf("|");
            if (sep <= 0) {
                return null;
            }
            
            String data = value.substring(sep + 1);
            String sign = value.substring(0, sep);
            
            String unsign = signer.sign(data).equals(sign) ? data : null;
            if (unsign == null) {
                return null;
            }

            return DataCodec.decode(unsign);
        };
        
        Function<Map<String, String>, String> encoder = attributes -> {
            String encoded = DataCodec.encode(attributes);
            encoded = signer.sign(encoded) + "|" + encoded;
            return new String(Base64.getEncoder().withoutPadding().encode(encoded.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        };
        
        return signed(token, decoder, encoder);
    }
    
    /**
     * Creates a session store that save data into Cookie. Cookie data is (un)signed it using the given
     * decoder and encoder.
     *
     * @param token Token to use.
     * @param decoder Decoder to use.
     * @param encoder Encoder to use.
     * @return Cookie session store.
     */
    static SessionStore signed(SessionToken token, Function<String, Map<String, String>> decoder, Function<Map<String, String>, String> encoder) {
        return new SessionStore() {

            @Override
            public Session newSession(Context ctx) {
                // No id as we do not save the session
                return Session.create(ctx, null);//.setNew(true);
            }

            @Override
            public Session findSession(Context ctx) {
                String signed = token.findToken(ctx);
                if (signed == null) {
                    return null;
                }
                Map<String, String> attributes = decoder.apply(signed);
                if (attributes == null || attributes.size() == 0) {
                    return null;
                }
                return Session.create(ctx, signed, new HashMap<>(attributes));//.setNew(false);
            }

            @Override
            public void deleteSession(Context ctx, Session session) {
                token.deleteToken(ctx, null);
            }

            @Override
            public void touchSession(Context ctx, Session session) {
                token.saveToken(ctx, encoder.apply(session.data()));
            }

            @Override
            public void saveSession(Context ctx, Session session) {
                // no saving internally
            }

            @Override
            public void renewSessionToken(Context ctx, Session session) {
                token.saveToken(ctx, encoder.apply(session.data()));
            }

        };
    }
}
