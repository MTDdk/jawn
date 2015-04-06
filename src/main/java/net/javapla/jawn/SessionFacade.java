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
package net.javapla.jawn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Facade to HTTP session. 
 *
 * @author Igor Polevoy
 */
public class SessionFacade implements Map<String, Object> {
    
    private final HttpServletRequest request;
    public SessionFacade(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Retrieve object from session.
     *
     * @param name name of object.
     * @return named object. 
     */
    public Object get(String name){
        return request.getSession(true).getAttribute(name);
    }

    /**
     * Convenience method, will do internal check for null and then cast. Will throw  <code>ClassCastException</code>
     * only in case the object in session is different type than expected.
     *
     * @param name name of session object.
     * @param type expected type
     * @param <T> type of the returned object
     * @return object in session, or null if not found. 
     */
    public <T> T get(String name, Class<T> type){
        return get(name) != null? type.cast(get(name)) : null;
    }


    /**
     * Removes object from session.
     * @param name name of object
     */
    public void remove(String name){
        request.getSession(true).removeAttribute(name);
    }

    /**
     * Add object to a session.
     * @param name name of object
     * @param value object reference.
     * @return the previous bound value for the name
     */
    public Object put(String name, Serializable value){
        Object val = request.getSession(true).getAttribute(name);
        request.getSession(false).setAttribute(name, value);
        return val;
    }

    /**
     * Returns time when session was created. 
     *
     * @return time when session was created.
     */
    public long getCreationTime(){
        return request.getSession(true).getCreationTime();
    }

    /**
     * Invalidates current session. All attributes are discarded.
     */
    public void invalidate(){
        request.getSession(true).invalidate();
    }

    /**
     * Sets time to live in seconds.
     * @param seconds time to live.
     */
    public void setTimeToLive(int seconds){
        request.getSession(true).setMaxInactiveInterval(seconds);
    }

    /**
     * Returns names of current attributes as a list.
     *
     * @return names of current attributes as a list.
     */
    public String[] names(){
        List<String> namesList = new ArrayList<String>();
        Enumeration<String> names = request.getSession(true).getAttributeNames();
        while (names.hasMoreElements()) {
            Object o = names.nextElement();
            namesList.add(o.toString());
        }        
        return namesList.toArray(new String[namesList.size()]);
    }


    /**
     * returns ID of the underlying session
     *
     * @return ID of the underlying session
     */
    public String getId(){
        return request.getSession(true).getId();
    }


    /**
     * Destroys current session
     */
    public void destroy(){
        request.getSession(true).invalidate();
    }



    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return !request.getSession(true).getAttributeNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        return request.getSession(true).getAttribute(key.toString()) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Enumeration<String> names = request.getSession(true).getAttributeNames();
        while (names.hasMoreElements()){
            String name = names.nextElement().toString();
            if(name.equals(value)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        return get(key.toString());
    }

    @Override
    public Object put(String key, Object value) {
        Object val = get(key.toString());
        put(key.toString(), (Serializable)value);
        return val;
    }

    @Override
    public Object remove(Object key) {
        Object val = get(key.toString());
        request.getSession(true).removeAttribute(key.toString());
        return val;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        Set<? extends String> keys = m.keySet();
        for(String k:keys){
            put(k, m.get(k));
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        Enumeration<String> names = request.getSession(true).getAttributeNames();
        while (names.hasMoreElements()){
            String name = names.nextElement();
            keys.add(name);
        }
        return keys;
    }

    @Override
    public Collection<Object> values() {
        Set<Object> values = new HashSet<Object>();
        Enumeration<String> names = request.getSession(true).getAttributeNames();
        while (names.hasMoreElements()){
            String name = names.nextElement();
            values.add(get(name));
        }
        return values;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
