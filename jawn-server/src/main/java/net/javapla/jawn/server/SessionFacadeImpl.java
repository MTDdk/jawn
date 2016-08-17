package net.javapla.jawn.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import net.javapla.jawn.core.http.SessionFacade;

/**
 * Facade to HTTP session. 
 *
 * @author Igor Polevoy
 * @author MTD
 * @deprecated Use {@link Session}
 */
@Deprecated
public class SessionFacadeImpl implements SessionFacade {
    
    private final HttpSession session;
    public SessionFacadeImpl(HttpSession session) throws IllegalArgumentException {
        if (session == null) 
            throw new IllegalArgumentException("The inputted HttpSession must not be null");
        this.session = session;
    }

    /**
     * Retrieve object from session.
     *
     * @param name name of object.
     * @return named object. 
     */
    public Object get(String name){
//        HttpSession session = session/*request.getSession(false)*/;
//        if (session == null) return null;
        return session.getAttribute(name);
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
//        HttpSession session = request.getSession(false);
//        if (session != null)
            session.removeAttribute(name);
    }

    /**
     * Add object to a session.
     * @param name name of object
     * @param value object reference.
     * @return the previous bound value for the name
     */
    public Object put(String name, Serializable value){
//        Object val = session/*request.getSession(true)*/.getAttribute(name);
//        request.getSession(false).setAttribute(name, value);
        Object val = session.getAttribute(name);
        session.setAttribute(name, value);
        return val;
    }

    /**
     * Returns time when session was created. 
     *
     * @return time when session was created.
     */
    public long getCreationTime() {
//        return request.getSession(true).getCreationTime();
        return session.getCreationTime();
    }
    
    @Override
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    /**
     * Invalidates current session. All attributes are discarded.
     */
    public void invalidate(){
//        HttpSession session = request.getSession(false);
//        if (session != null)
            session.invalidate();
    }

    /**
     * Sets time to live in seconds.
     * @param seconds time to live.
     */
    public void setTimeToLive(int seconds){
//        request.getSession(true).setMaxInactiveInterval(seconds);
        session.setMaxInactiveInterval(seconds);
    }
    
    @Override
    public int getTimeToLive() {
        return session.getMaxInactiveInterval();
    }
    
    @Override
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }
    
    @Override
    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }
    
    @Override
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }
    
    /**
     * Returns names of current attributes as a list.
     *
     * @return names of current attributes as a list.
     */
    public String[] names(){
        List<String> namesList = new ArrayList<String>();
        Enumeration<String> names = session.getAttributeNames();//request.getSession(true).getAttributeNames();
        if (names == null) return null;
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
        return session/*request.getSession(true)*/.getId();
    }


    /**
     * Destroys current session
     */
    public void destroy(){
        session/*request.getSession(true)*/.invalidate();
    }



    @Override
    public boolean isEmpty() {
//        HttpSession session = request.getSession(false);
        return session == null || !session.getAttributeNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
//        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(key.toString()) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Enumeration<String> names = session/*request.getSession(true)*/.getAttributeNames();
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
        session/*request.getSession(true)*/.setAttribute(key, value);
        return val;
    }

    @Override
    public Object remove(Object key) {
        Object val = get(key.toString());
//        HttpSession session = request.getSession(false);
//        if (session == null) return null;
        session.removeAttribute(key.toString());
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
        Enumeration<String> names = session/*request.getSession(true)*/.getAttributeNames();
        while (names.hasMoreElements()){
            String name = names.nextElement();
            keys.add(name);
        }
        return keys;
    }

    @Override
    public Collection<Object> values() {
        Set<Object> values = new HashSet<Object>();
        Enumeration<String> names = session/*request.getSession(true)*/.getAttributeNames();
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

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }
}
