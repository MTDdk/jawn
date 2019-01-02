package net.javapla.jawn.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

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
    
    public MultiList(Map<String, List<T>> map) {
        this();
        map.forEach(this::put);
    }
    
    public MultiList(List<Entry<String, T>> entries) {
        this();
        entries.forEach(entry -> put(entry.getKey(),entry.getValue()));
    }
    
    @SafeVarargs
    public final MultiList<T> put(final String key, T... param) {
        parts.putIfAbsent(key, new ArrayList<T>());
        
        for (T p : param) {
            parts.get(key).add(p);
        }
        
        return this;
    }
    
    public final MultiList<T> put(final String key, Collection<T> params) {
        parts.putIfAbsent(key, new ArrayList<T>());
        
        for (T p : params) {
            parts.get(key).add(p);
        }
        
        return this;
    }
    
    public final MultiList<T> put(final String key, T param) {
        parts.putIfAbsent(key, new ArrayList<T>());
        parts.get(key).add(param);
        
        return this;
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
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public MultiList<T> orElse(Supplier<MultiList<T>> sup) {
        return sup.get();
    }
    
    public MultiList<T> orElse(MultiList<T> multilist) {
        return multilist;
    }
    
    @Override
    public String toString() {
        return parts.toString();
    }
    
}
