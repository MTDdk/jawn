package net.javapla.jawn.core.http;

import java.io.Serializable;
import java.util.Map;

/**
 * Facade to HTTP session. 
 *
 * @author MTD
 */
public interface SessionFacade extends Map<String, Object> {
    
    /**
     * Retrieve object from session.
     *
     * @param name name of object.
     * @return named object. 
     */
    public Object get(String name);

    /**
     * Convenience method, will do internal check for null and then cast. Will throw  <code>ClassCastException</code>
     * only in case the object in session is different type than expected.
     *
     * @param name name of session object.
     * @param type expected type
     * @param <T> type of the returned object
     * @return object in session, or null if not found. 
     */
    public <T> T get(String name, Class<T> type);


    /**
     * Removes object from session.
     * @param name name of object
     */
    public void remove(String name);

    /**
     * Add object to a session.
     * @param name name of object
     * @param value object reference.
     * @return the previous bound value for the name
     */
    public Object put(String name, Serializable value);

    /**
     * Returns time when session was created. 
     *
     * @return time when session was created.
     */
    public long getCreationTime();

    /**
     * Invalidates current session. All attributes are discarded.
     */
    public void invalidate();

    /**
     * Sets time to live in seconds.
     * @param seconds time to live.
     */
    public void setTimeToLive(int seconds);

    /**
     * Returns names of current attributes as a list.
     *
     * @return names of current attributes as a list.
     */
    public String[] names();


    /**
     * returns ID of the underlying session
     *
     * @return ID of the underlying session
     */
    public String getId();


    /**
     * Destroys current session
     */
    public void destroy();

}
