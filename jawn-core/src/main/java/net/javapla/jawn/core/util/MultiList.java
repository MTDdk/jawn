package net.javapla.jawn.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author MTD
 *
 */
public class MultiList<T> {

    /**
     * fieldName -> Item
     */
    private final Map<String, List<T>> parts;
    
    public MultiList() {
        this.parts = new HashMap<String, List<T>>();
    }
    
    @SafeVarargs
    public final void put(String key, T... param) {
        if (!parts.containsKey(key)) {
            parts.put(key, new ArrayList<T>());
        }
        
        for (T p : param) {
            parts.get(key).add(p);
        }
    }
    
    public final void put(String key, Collection<T> params) {
        parts.putIfAbsent(key, new ArrayList<T>());
        
        for (T p : params) {
            parts.get(key).add(p);
        }
    }
    
    /**
     * Returns the <code>FormItem</code> of the given name.
     * @param param
     * @return The <code>FormItem</code> or null, if not found
     */
    public T first(String param) {
        List<T> items = parts.get(param);
        if (items == null) return null;
        return items.get(0);
    }
    
    public T last(String param) {
        List<T> items = parts.get(param);
        if (items == null) return null;
        return items.get(items.size()-1);
    }
    
    /**
     * Returns the list of <code>String</code> with the given name.
     * @param param
     * @return The <code>List&lt;String&gt;</code> or null, if not found
     */
    public List<T> list(String param) {
        return parts.get(param);
    }
    
    public boolean contains(String key) {
        return parts.containsKey(key);
    }
    
    
    public Set<String> keySet() {
        return parts.keySet();
    }
    
    public int size() {
        return parts.size();
    }
    
    @Override
    public String toString() {
        return parts.toString();
    }
    
}
