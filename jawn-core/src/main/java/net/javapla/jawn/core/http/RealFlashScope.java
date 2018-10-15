package net.javapla.jawn.core.http;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.DataCodec;

public class RealFlashScope implements FlashScope {
    
    private final String flashCookieName;
    
    private final HashMap<String,String> currentFlashCookieData = new HashMap<>();
    private final HashMap<String,String> outgoingFlashCookieData = new HashMap<>();

    private boolean initialised = false;;

    @Inject
    public RealFlashScope(JawnConfigurations properties) {
        flashCookieName = properties.getSecure(Constants.PROPERTY_COOKIE_PREFIX).orElse("JAWN") + Constants.FLASH_SUFFIX;
    }

    @Override
    public void init(Context context) {
        Cookie cookie = context.getCookie(flashCookieName);
        
        if (cookie != null) {
            DataCodec.decode(currentFlashCookieData, cookie.getValue());
        }
        
        initialised = true;
    }
    
    @Override
    public boolean isInitialised() {
        return initialised;
    }

    @Override
    public void save(Context context) {
        if (outgoingFlashCookieData.isEmpty()) {
            // only need to build a cookie (to empty its contents) if one currently exists
            if (context.hasCookie(flashCookieName)) {

                // build empty flash cookie
                context.addCookie(
                    Cookie.builder(flashCookieName, "")
                        .setPath(context.contextPath() + "/")
                        .setSecure(false)
                        .setMaxAge(0)
                        .build());
            }
        } else {
            // build a cookie with this flash data
            String flashData = DataCodec.encode(outgoingFlashCookieData);

            Cookie.Builder cookie = Cookie.builder(flashCookieName, flashData);
            cookie.setPath(context.contextPath() + "/");
            cookie.setSecure(false);
            // "-1" does not set "Expires" for that cookie
            // => Cookie will live as long as the browser is open theoretically
            cookie.setMaxAge(-1);

            context.addCookie(cookie.build());
        }
    }
    
    private void validateKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Flash key may not be null");
        }
        if (key.contains(":")) {
            throw new IllegalArgumentException("Flash key may not contain character ':'");
        }
    }
    
    @Override
    public void now(String key, String value) {
        this.validateKey(key);
        currentFlashCookieData.put(key, value);
    }
    
    @Override
    public String get(String key) {
        this.validateKey(key);
        return currentFlashCookieData.get(key);
    }

    @Override
    public boolean remove(String key) {
        this.validateKey(key);
        this.outgoingFlashCookieData.remove(key);
        return currentFlashCookieData.remove(key) != null;
    }
    
    @Override
    public boolean contains(String key) {
        this.validateKey(key);
        return currentFlashCookieData.containsKey(key);
    }
    
    @Override
    public void put(String key, String value) {
        this.validateKey(key);
        currentFlashCookieData.put(key, value);
        outgoingFlashCookieData.put(key, value);
    }

    @Override
    public void discard(String key) {
        this.validateKey(key);
        outgoingFlashCookieData.remove(key);
    }

    @Override
    public void discard() {
        outgoingFlashCookieData.clear();
    }

    @Override
    public void keep(String key) {
        this.validateKey(key);
        if (currentFlashCookieData.containsKey(key)) {
            outgoingFlashCookieData.put(key, currentFlashCookieData.get(key));
        }
    }

    @Override
    public void keep() {
        outgoingFlashCookieData.putAll(currentFlashCookieData);
    }

    @Override
    public void clearCurrentFlashCookieData() {
        currentFlashCookieData.clear();
    }

    @Override
    public Map<String,String> getCurrentFlashCookieData() {
        return currentFlashCookieData;
    }

    @Override
    public Map<String,String> getOutgoingFlashCookieData() {
        return outgoingFlashCookieData;
    }
}
