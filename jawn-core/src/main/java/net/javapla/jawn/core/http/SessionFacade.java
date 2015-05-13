/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package net.javapla.jawn.core.http;

import java.io.Serializable;
import java.util.Map;

/**
 * Facade to HTTP session. 
 *
 * @author Igor Polevoy
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
