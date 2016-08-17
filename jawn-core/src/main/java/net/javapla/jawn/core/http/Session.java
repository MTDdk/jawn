package net.javapla.jawn.core.http;

import java.util.Map;

public interface Session {

    void init(Context context);
    
    String getId();
    
    Map<String, Object> getData();
    
    void put(String key, Object value);
    
    Object get(String key);
    <T> T get(String key, Class<T> type);
    
    Object remove(String key);
    <T> T remove(String key, Class<T> type);
    
    void invalidate(); //clear
    
    boolean isEmpty();
    
    void setExpiryTime(long expiryTimeMS);
    
    void save(Context context);
    
    //TODO authencity tokens for verification purposes
}
