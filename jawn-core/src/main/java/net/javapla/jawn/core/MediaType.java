package net.javapla.jawn.core;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.javapla.jawn.core.util.StringUtil;

public class MediaType implements Comparable<MediaType> {
    
    public static final String xml = "application/xml";
    public static final String xml_like = "application/*+xml";
    public static final String json = "application/json";
    public static final String json_like = "application/*+json";
    public static final String form = "application/x-www-form-urlencoded";
    public static final String multipart = "multipart/form-data";
    public static final String octet_stream = "application/octet-stream";
    public static final String text = "text/plain";
    public static final String html = "text/html";
    public static final String WILDCARD_TYPE = "*";
    /**
     * The media type {@code charset} parameter name.
     */
    public static final String CHARSET_PARAMETER = "charset";
    
    private static final String UTF_8 = StandardCharsets.UTF_8.name().toLowerCase();
    
    /** application/xml */
    public final static MediaType XML = new MediaType(xml, UTF_8);
    /** application/*+xml */
    public final static MediaType XML_LIKE = new MediaType(xml_like, UTF_8);
    /** application/json */
    public final static MediaType JSON = new MediaType(json, UTF_8);
    /** application/*+json */
    public final static MediaType JSON_LIKE = new MediaType(json_like, UTF_8);
    /** application/x-www-form-urlencoded */
    public final static MediaType FORM = new MediaType(form, UTF_8);
    /** multipart/form-data */
    public final static MediaType MULTIPART = new MediaType(multipart, UTF_8);
    /** application/octet-stream */
    public final static MediaType OCTET_STREAM = new MediaType(octet_stream, null);
    /** text/plain */
    public final static MediaType TEXT = new MediaType(text, UTF_8);
    /** text/html */
    public final static MediaType HTML = new MediaType(html, UTF_8);
    ///** text/* */
    //public final static MediaType TEXT = new MediaType("text", WILDCARD_TYPE);
    /**
     * Server sent events media type.
     */
    /** text/event-stream */
    public static final MediaType SERVER_SENT_EVENTS = new MediaType("text/event-stream", null);
    /** {@link WILDCARD_TYPE}/{@link WILDCARD_TYPE} */
    public final static MediaType WILDCARD = new MediaType(WILDCARD_TYPE + "/" + WILDCARD_TYPE, null);
    
    //public final static List<MediaType> ALL = Collections.singletonList(WILDCARD);
    
    /**
     * Alias for most used types.
     */
    private static final Map<String, MediaType> cache = new ConcurrentHashMap<>();
    static {
        cache.put(HTML.subtype(), HTML);
        cache.put(HTML.value, HTML);
        cache.put("json", JSON);
        cache.put("octetstream", OCTET_STREAM);
        cache.put("form", FORM);
        cache.put("multipart", MULTIPART);
        cache.put("xml", XML);
        cache.put("plain", TEXT);
        cache.put("*", WILDCARD);
    }
    

    private final String raw, value;
    private final int subtypeStart, subtypeEnd;
    private final String charset;
    
    
    private MediaType(String raw, String charset) throws Up.BadMediaType {
        this.raw = raw;
        
        this.subtypeStart = raw.indexOf('/');
        if (subtypeStart < 1) throw Up.BadMediaType(raw);
        
        int subtypeEnd = raw.indexOf(';', subtypeStart);
        if (subtypeEnd < subtypeStart) {
            this.value = raw;
            this.subtypeEnd = raw.length();
        } else {
            this.value = raw.substring(0, subtypeEnd);
            this.subtypeEnd = subtypeEnd;
        }
        
        String param = parameter("charset");
        this.charset = param == null ? charset : param;
    }
    
    public String type() {
        return raw.substring(0, subtypeStart);
    }
    
    public String subtype() {
        return raw.substring(subtypeStart + 1, subtypeEnd);
    }
    
    public final String value() {
        return value;
    }
    
    public String charset() {
        return this.charset;
    }
    
    public float quality() {
        String q = parameter("q");
        return q == null ? 1f : Float.parseFloat(q);
    }
    
    /**
     * Get the parameter for this type or <code>null</code>
     * 
     * Examples:
     * <ul>
     * <li><code>Content-Type: text/html; charset=utf-8</code>
     * <li><code>Content-Type: multipart/form-data; boundary=something</code>
     * <li><code>Accept : text/html, application/xml;q=0.9, *{@literal /}*;q=0.8</code>
     * 
     * @param name
     * @return value of <code>null</code>
     */
    public String parameter(String name) {
        int paramNameStart = subtypeEnd + 1;
        
        do {
            int paramNameEnd = raw.indexOf('=', paramNameStart);
            if (paramNameEnd > paramNameStart) {
                String paramName = raw.substring(paramNameStart, paramNameEnd).trim();
                int paramValueEnd = raw.indexOf(';', paramNameEnd);
                if (paramValueEnd < paramNameEnd) paramValueEnd = raw.length();
                
                if (name.equals(paramName)) {
                    return raw.substring(paramNameEnd + 1, paramValueEnd);
                }
                
                paramNameStart = paramValueEnd + 1;
            }
        } while (paramNameStart < raw.length());
        
        return null;
    }

    private int parameterCount() {
        int count = 0;
        for (int i = subtypeEnd; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '=') count++;
        }
        return count;
    }
    
    public boolean isTextual() {
        if ("text".equals(type())) return true;
        
        String subtype = subtype();
        return "json".equals(subtype)
            || "javascript".equals(subtype)
            || "xml".equals(subtype)
            ;
    }
    
    public static MediaType valueOf(final String type) throws Up.BadMediaType {
        if (type == null || type.isBlank() || (type.length() == 1 && type.charAt(0) == '*')) return WILDCARD;
        
        return cache.computeIfAbsent(type, t -> new MediaType(t, null));
    }
    
    /**
     * Converts a comma-separated string into a list of MediaTypes 
     */
    public static List<MediaType> parse(final String types) throws Up.BadMediaType {
        LinkedList<MediaType> result = new LinkedList<>();
        
        StringUtil.split(types, ',', raw -> {
            String type = raw.trim();
            if (!type.isBlank())
                result.add(valueOf(type));
        });
        
        return result;
    }
    
    public boolean matches(final MediaType that) {
        if (this == that /*|| this.wildcardType || that.wildcardType*/) {
            // same or */*
            return true;
        }
        if (type().equals(that.type())) {
            if (subtype().equals(that.subtype()) /*|| this.wildcardSubtype || that.wildcardSubtype*/) {
                return true;
            }
            if (subtype().startsWith("*+")) {
                return that.subtype().endsWith(subtype().substring(2));
            }
            if (subtype().startsWith(WILDCARD_TYPE)) {
                return that.subtype().endsWith(subtype().substring(1));
            }
        }
        return false;
    }
    
    public boolean matches(final String contentType) {
        return matches(valueOf(contentType));
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MediaType) {
            MediaType that = (MediaType) obj;
            
            // #hashCode is not always sufficient, as the content-type might have parameters appended
            //return this.hashCode() == that.hashCode();
            
            return type().equals(this.type()) && subtype().equals(that.subtype()); 
            // && parameters.equals(that.parameters);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return raw.hashCode();
    }
    
    private int score() {
        int precedence = 0;
        
        return precedence;
    }
    
    
    @Override
    public int compareTo(MediaType that) {
        if (this == that) {
            return 0;
        }
        
        int diff = that.score() - score();
        if (diff == 0) {
            diff = Float.compare(that.quality(), quality());
            if (diff == 0)
                return that.parameterCount() - parameterCount();
        }
        
        return diff;
    }

    @Override
    public String toString() {
        return raw;
    }
    
    /*private static List<MediaType> _parse(final String value) {
        String[] types = StringUtil.split(value, ',');
        List<MediaType> result = new ArrayList<>(types.length);
        for (String type : types) {
            String[] parts = type.trim().split(";");
            if (parts[0].equals(WILDCARD_TYPE)) {
                // odd and ugly media type
                result.add(WILDCARD);
            } else {
                String[] typeAndSubtype = parts[0].split("/");
                if (typeAndSubtype.length != 2) {
                    throw new Up.BadMediaType(value);
                }
                String stype = typeAndSubtype[0].trim();
                String subtype = typeAndSubtype[1].trim();
                if (WILDCARD_TYPE.equals(stype) && !WILDCARD_TYPE.equals(subtype)) {
                    throw new Up.BadMediaType(value);
                }
                Map<String, String> parameters = createParametersMap(null);
                if (parts.length > 1) {
                    for (int i = 1; i < parts.length; i++) {
                        String[] parameter = parts[i].split("=");
                        if (parameter.length > 1) {
                            parameters.put(parameter[0].trim(), parameter[1].trim().toLowerCase());
                        }
                    }
                }
                result.add(new MediaType(stype, subtype, parameters));
            }
        }
        if (result.size() > 1) {
            Collections.sort(result);
        }
        return result;
    }*/

    public static MediaType byPath(final String path) {
        int last = path.lastIndexOf('.');
        if (last != -1) {
            String ext = path.substring(last + 1);
            return byExtension(ext);
        }
        return OCTET_STREAM;
    }

    public static MediaType byExtension(final String ext) {
        switch(ext) {
            
            case "gif": return valueOf("image/gif");
            case "jpg": return valueOf("image/jpg");
            case "jpeg":
            case "jpe": 
                return valueOf("image/jpeg");
            case "png": return valueOf("image/png");
            case "svg": return valueOf("image/svg+xml");
            
            case "txt": return TEXT;
            case "css": 
            case "scss": 
                return valueOf("text/css");
            case "js": 
            case "coffee": 
                return valueOf("text/javascript");
            case "mjs": return valueOf("text/javascript");
            
            case "mp4": return valueOf("video/mp4");
            case "webm": return valueOf("video/webm");
            case "mpeg": return valueOf("video/mpeg");
            
            case "json": return JSON;
            case "xml": return XML;
            case "zip": return valueOf("application/zip");
            case "gz":
            case "gzip": 
                return valueOf("application/gzip");
            case "otf": valueOf("application/x-font-opentype");
            case "ttf": valueOf("application/x-font-truetype");
            case "woff": valueOf("application/font-woff");
            case "woff2": valueOf("application/font-woff2");
        }
        
        return OCTET_STREAM;
    }
}