package net.javapla.jawn.core;

import java.time.Duration;

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

}
