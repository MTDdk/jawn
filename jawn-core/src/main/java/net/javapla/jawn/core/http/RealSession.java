package net.javapla.jawn.core.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.inject.Inject;

import net.javapla.jawn.core.cache.Cache;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.crypto.Crypto;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.DataCodec;
import net.javapla.jawn.core.util.StringUtil;

//SessionImpl
public class RealSession implements Session {
    
    private final Crypto crypto;
    private final Cache cache;
    private final HashMap<String, String> data = new HashMap<>();
    private HashMap<String, Object> cachedData;

    
    private long sessionExpiryTimeInMs;
    private final long defaultSessionExpiryTimeInMs;
    private final boolean cookieOnlySession;
    private final String sessionCookieName;
    private final String applicationSecret; //TODO probably needs to be stored somewhere else (perhaps the Crypto)
    private final boolean applicationCookieEncryption;
    
    private boolean sessionDataHasChanged = false;

    @Inject
    public RealSession(Crypto crypto, JawnConfigurations properties, Cache cache) {
        this.crypto = crypto;
        this.cache = cache;
        
        //TODO read a bunch of properties
        sessionExpiryTimeInMs = -1; // TODO should be configurable
        defaultSessionExpiryTimeInMs = sessionExpiryTimeInMs * 1000; //TODO should be configurable
        
        cookieOnlySession = properties.getBooleanSecure(Constants.PROPERTY_SESSION_COOKIE_ONLY).orElse(false); // if false have the cookie only save a session id that we then look up
        sessionCookieName = properties.getSecure(Constants.PROPERTY_COOKIE_PREFIX).orElse("JAWN") + Constants.SESSION_SUFFIX;
        
        Optional<String> secret = properties.getSecure(Constants.PROPERTY_SECURITY_SECRET);
        if (secret.isPresent()) {
            applicationSecret = secret.get();
            applicationCookieEncryption = properties.getBoolean(Constants.PROPERTY_SECURITY_COOKIE_ENCRYPTION);
        } else {
            applicationSecret = "";
            applicationCookieEncryption = false;
        }
    }

    @Override
    public synchronized void init(Context context) {
        Cookie cookie = context.getCookie(sessionCookieName);
        
        if (cookie != null && !StringUtil.blank(cookie.getValue())) {
            String value = cookie.getValue();
            
            // the first substring until "-" is the SHA signing
            String signing = value.substring(0, value.indexOf("-"));

            // rest from "-" until the end is the payload of the cookie
            String payload = value.substring(value.indexOf("-") + 1);

            
            // check if payload is valid:
            if (DataCodec.safeEquals(signing, crypto.hash().SHA256().sign(payload, applicationSecret))) {
                if (applicationCookieEncryption)
                    payload = crypto.encrypt().AES().decrypt(payload);
                DataCodec.decode(data, payload);
                
                // If an expiry time was set previously use that instead of the
                // default session expire time.
                if (data.containsKey(EXPIRY_TIME_KEY)) {
                    Long expiryTime = Long.parseLong(data.get(EXPIRY_TIME_KEY));
                    if (expiryTime >= 0) {
                        sessionExpiryTimeInMs = expiryTime;
                    }
                }
                
                checkExpire();
            }
        }
        
        cachedData = cache.computeIfAbsent(SESSION_KEY + getId(), () -> new HashMap<>());
    }
    
    @Override
    public synchronized boolean isInitialised() {
        return cachedData != null;
    }

    @Override
    public String getId() {
        if (!data.containsKey(ID_KEY)) {
            String uuid = UUID.randomUUID().toString();
            put(ID_KEY, uuid);
            return uuid;
        }

        return get(ID_KEY);
    }

    @Override
    public Map<String, Object> getData() {
        if (cookieOnlySession)
            return Collections.unmodifiableMap(data);
        return Collections.unmodifiableMap(cachedData);
    }

    @Override
    public void put(String key, Object value) {
        if (StringUtil.contains(key, ':')) throw new IllegalArgumentException(); //TODO
        
        sessionDataHasChanged = true;
        
        if (value == null) {
            remove(key);
        } else {
            if (cookieOnlySession)
                data.put(key, value.toString()); // TODO make sure to think about how methods should behave if cookieOnlySession (maps will only hold strings)
            else
                cachedData.put(key, value);
        }
    }

    @Override
    public String get(String key) {
        String val = data.get(key);
        if (val != null) return val;
        
        Object obj = cachedData.get(key);
        return obj == null ? null : obj.toString();
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        Object o = data.get(key);
        if (o == null) o = cachedData.get(key);
        return o == null ? null : type.cast(o);
    }

    @Override
    public Object remove(String key) {
        Object o;
        return (o = data.remove(key)) != null ? o : cachedData.remove(key);
    }

    @Override
    public <T> T remove(String key, Class<T> type) {
        Object o;
        return (o = remove(key)) == null ? null : type.cast(o);
    }
    
    @Override
    public boolean containsKey(String key) {
        return data.containsKey(key) || cachedData.containsKey(key);
    }

    @Override
    public void invalidate() {
        sessionDataHasChanged = true;
        data.clear();
        cachedData.clear();
    }

    @Override
    public boolean isEmpty() {
        int itemsToIgnore = 0;
        if (data.containsKey(TIMESTAMP_KEY)) {
            itemsToIgnore++;
        }
        if (data.containsKey(EXPIRY_TIME_KEY)) {
            itemsToIgnore++;
        }
        return (data.isEmpty() || data.size() + cachedData.size() == itemsToIgnore);
    }

    @Override
    public void setExpiryTime(long expiryTimeMs) {
        if (expiryTimeMs > 0) {
            data.put(EXPIRY_TIME_KEY, "" + expiryTimeMs);
            sessionExpiryTimeInMs = expiryTimeMs;
        } else {
            data.remove(EXPIRY_TIME_KEY);
            sessionExpiryTimeInMs = defaultSessionExpiryTimeInMs;
            sessionDataHasChanged = true;
        }
        
        if (sessionExpiryTimeInMs > 0) {
            if (!data.containsKey(TIMESTAMP_KEY)) {
                data.put(TIMESTAMP_KEY, "" + System.currentTimeMillis());
            }
            checkExpire();
            sessionDataHasChanged = true;
        }
    }

    @Override
    public void save(Context context) {
        if (!sessionDataHasChanged && sessionExpiryTimeInMs <= 0) return;
        
        if (isEmpty()) {
            if (context.hasCookie(sessionCookieName)) {
                Cookie cookie = createApplicationCookie("", context).build();
                context.addCookie(cookie);
            }
            return;
        }
        
        if (sessionExpiryTimeInMs > 0 && !data.containsKey(TIMESTAMP_KEY)) {
            data.put(TIMESTAMP_KEY, Long.toString(System.currentTimeMillis()));
        }
        
        String sessionData = DataCodec.encode(data);
        // first encrypt data and then generate HMAC from encrypted data
        // http://crypto.stackexchange.com/questions/202/should-we-mac-then-encrypt-or-encrypt-then-mac
        if (applicationCookieEncryption)
            sessionData = crypto.encrypt().AES().encrypt(sessionData);
        String signing = crypto.hash().SHA256().sign(sessionData, applicationSecret);
        
        Cookie.Builder cookieBuilder = createApplicationCookie(signing + "-" + sessionData, context);
        if (sessionExpiryTimeInMs > 0) {
            cookieBuilder.setMaxAge((int) (sessionExpiryTimeInMs / 1000));
        }
        context.addCookie(cookieBuilder.build());
    }
    
    private static final String TIMESTAMP_KEY = "__TK";
    private static final String EXPIRY_TIME_KEY = "__ETK";
    private static final String ID_KEY = "___ID";
    private static final String SESSION_KEY = "__SESSION_";
    protected boolean shouldExpire() {
        if (sessionExpiryTimeInMs > 0) {
            if (!data.containsKey(TIMESTAMP_KEY)) {
                return true;
            }
            
            long timestamp = Long.parseLong(data.get(TIMESTAMP_KEY));
            return timestamp + sessionExpiryTimeInMs < System.currentTimeMillis();
        }
        return false;
    }
    
    private void checkExpire() {
        if (sessionExpiryTimeInMs > 0) {
            if (shouldExpire()) {
                sessionDataHasChanged = true;
                data.clear();
            } else {
                // Seems okay - prolong session
                data.put(TIMESTAMP_KEY, "" + System.currentTimeMillis());
            }
        }
    }

    private Cookie.Builder createApplicationCookie(String value, Context context) {
        return Cookie.builder(sessionCookieName, value)
                .setPath(context.contextPath() + "/")
                //.setDomain(domain)
                //.setSecure(secure)
                //.setHttpOnly(httpOnly)
                ;
    }
}
