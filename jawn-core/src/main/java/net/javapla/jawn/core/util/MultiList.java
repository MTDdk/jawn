package net.javapla.jawn.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        parts.putIfAbsent(key, new ArrayList<T>(1));
        
        for (T p : param) {
            parts.get(key).add(p);
        }
        
        return this;
    }
    
    public final MultiList<T> put(final String key, Collection<T> params) {
        parts.putIfAbsent(key, new ArrayList<T>(1));
        
        for (T p : params) {
            parts.get(key).add(p);
        }
        
        return this;
    }
    
    public final MultiList<T> put(final String key, T param) {
        parts.putIfAbsent(key, new ArrayList<T>(1));
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
    
    public Optional<T> firstOptionally(final String param) {
        return Optional.ofNullable(first(param));
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
    
    public <U> MultiList<U> map(Function<T, U> mapper) {
        MultiList<U> newList = new MultiList<>();
        
        parts.forEach((key, oldList) -> newList.put(key, oldList.stream().map(mapper).collect(Collectors.toList())));
        
        return newList;
    }
    
    public boolean contains(String key) {
        return parts.containsKey(key);
    }
    
    public Set<String> keySet() {
        return parts.keySet();
    }
    
    public <K,V> void mapAndConsume(Function<List<T>, V> mapper, BiConsumer<String, V> consumer) {
        parts.forEach((key, values) -> consumer.accept(key, mapper.apply(values)));
    }
    
    public int size() {
        return parts.size();
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public String toString() {
        return parts.toString();
    }
    
    
    public static <T> MultiList<T> empty() {
        return new MultiList<>();
    }
}