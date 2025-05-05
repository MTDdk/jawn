package net.javapla.jawn.core;

import java.util.HashMap;
import java.util.Map;

public record View(String view, Map<String, Object> data) {
    // data = view model
    public View(String view) {
        this(view, new HashMap<>());
    }
    
    public View put(String key, Object value) {
        data.put(key, value);
        return this;
    }
}