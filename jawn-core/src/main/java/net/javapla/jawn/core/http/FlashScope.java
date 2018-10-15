package net.javapla.jawn.core.http;

import java.util.Map;

/**
 * (From NinjaFramework)
 * 
 * Flash Scope consists of two kinds of data: "current" and "outgoing". Current
 * data will only exist for the current request.  Outgoing data will exist for
 * the current and next request.  Neither should be considered secure or encrypted.
 * Its useful for communicating error messages or form submission results.
 * 
 * A FlashScope is i18n aware and the values will be looked up for i18n translations
 * by template engines that support it.
 * 
 * If the Flash Scope has outgoing data then a cookie will be sent to the client
 * and will be valid on the next request. Stuff in a flash cookie gets deleted
 * after the next request.
 * 
 * If an incoming request has a flash cookie then the data from it will be 
 * loaded as "current" flash data.  Unless you keep() those keys that data will
 * only be valid for the current request.
 */
public interface FlashScope {

    void init(Context context);
    boolean isInitialised();
    void save(Context context);
    void now(String key, String value);
    String get(String key);
    boolean remove(String key);
    boolean contains(String key);
    void put(String key, String value);
    void discard(String key);
    void discard();
    void keep(String key);
    void keep();
    void clearCurrentFlashCookieData();
    Map<String, String> getCurrentFlashCookieData();
    Map<String, String> getOutgoingFlashCookieData();
}
