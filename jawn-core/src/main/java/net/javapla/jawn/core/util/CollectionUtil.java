package net.javapla.jawn.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CollectionUtil {
    
    /**
     * Reduces overhead compared to the far more advanced {@link #map(Object...)}.
     * 
     * @param key A single key
     * @param <K> type of the key
     * @param value A single value
     * @param <V> type of the value
     * @return A Modifiable Map with a single entry
     */
    public static final <K, V> Map<K, V> map(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Converting a list into a map. This implementation uses a {@linkplain TreeMap} which ensures
     * natural ordering of the keys
     * 
     * @param keyValues An even list of key/value pairs to be converted into a map
     * @param <K> type of the keys
     * @param <V> type of the values
     * 
     * @return The resulting map of the values
     * @throws IllegalArgumentException If the number of <code>keyValues</code> is not even or is
     *         null
     * @throws ClassCastException If the class of the specified key or value prevents it from being
     *         stored in this map
     */
    @SuppressWarnings("unchecked")
    public static final <K, V> Map<K, V> map(Object... keyValues ) throws IllegalArgumentException, ClassCastException {
        if (keyValues == null) throw new IllegalArgumentException("Arguments must not be null");
        if ((keyValues.length & 0b01) != 0) throw new IllegalArgumentException("number of arguments must be even");
        //             length % 2 != 0   ( - essentially; is the first bit a 1?)
        
        Map<K, V> map = new TreeMap<>();
        for (int i = 0; i < keyValues.length; i+=2) {
            K key = (K) keyValues[i];
            V value = (V) keyValues[i+1];
            map.put(key, value);
        }
        
        return map;
    }
    
}
