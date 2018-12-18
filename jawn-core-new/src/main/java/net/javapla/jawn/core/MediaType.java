package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MediaType implements Comparable<MediaType> {
    
    public static final String WILDCARD_TYPE = "*";
    /**
     * The media type {@code charset} parameter name.
     */
    public static final String CHARSET_PARAMETER = "charset";
    
    /** application/xml */
    public final static MediaType XML = new MediaType("application", "xml");
    /** application/*+xml */
    public final static MediaType XML_LIKE = new MediaType("application", "*+xml");
    /** application/json */
    public final static MediaType JSON = new MediaType("application", "json");
    /** application/*+json */
    public final static MediaType JSON_LIKE = new MediaType("application", "*+json");
    /** application/x-www-form-urlencoded */
    public final static MediaType FORM = new MediaType("application", "x-www-form-urlencoded");
    /** multipart/form-data */
    public final static MediaType MULTIPART = new MediaType("multipart", "form-data");
    /** application/octet-stream */
    public final static MediaType OCTET_STREAM = new MediaType("application", "octet-stream");
    /** text/plain */
    public final static MediaType PLAIN = new MediaType("text", "plain");
    /** text/html */
    public final static MediaType HTML = new MediaType("text", "html");
    /** text/* */
    public final static MediaType TEXT = new MediaType("text", WILDCARD_TYPE);
    /**
     * Server sent events media type.
     */
    /** text/event-stream */
    public static final MediaType SERVER_SENT_EVENTS = new MediaType("text", "event-stream");
    /** {@link WILDCARD_TYPE}/{@link WILDCARD_TYPE} */
    public final static MediaType WILDCARD = new MediaType(WILDCARD_TYPE, WILDCARD_TYPE);
    
    public final static List<MediaType> ALL = Collections.singletonList(WILDCARD);
    

    private final String type;
    private final String subtype;
    private final Map<String, String> parameters;
    
    private final String name;
    private final int hashcode;
    
    /**
     * True for wild-card types.
     */
    private final boolean wildcardType;

    /**
     * True for wild-card sub-types.
     */
    private final boolean wildcardSubtype;
    
    /**
     * Alias for most used types.
     */
    private static final HashMap<String, List<MediaType>> cache = new HashMap<>();

    static {
      cache.put("html", Collections.singletonList(HTML));//ImmutableList.of(html));
      cache.put("json", Collections.singletonList(JSON));
//      cache.put("css", Collections.singletonList(css));
//      cache.put("js", Collections.singletonList(js));
      cache.put("octetstream", Collections.singletonList(OCTET_STREAM));
      cache.put("form", Collections.singletonList(FORM));
      cache.put("multipart", Collections.singletonList(MULTIPART));
      cache.put("xml", Collections.singletonList(XML));
      cache.put("plain", Collections.singletonList(PLAIN));
      cache.put("*", ALL);
    }
    
    public MediaType(final String type, final String subtype, final Map<String, String> parameters) {
        this(type, subtype, null, createParametersMap(parameters));
    }
    
    public MediaType(final String type, final String subtype) {
        this(type, subtype, null, null);
    }
    
    public MediaType(final String type, final String subtype, final String charset) {
        this(type, subtype, charset, null);
    }
    
    /**
     * Creates a new instance of {@code MediaType}, both type and subtype are wildcards.
     * Consider using the constant {@link #WILDCARD_TYPE} instead.
     */
    public MediaType() {
        this(WILDCARD_TYPE, WILDCARD_TYPE, null, null);
    }
    
    private MediaType(final String type, final String subtype, final String charset, final Map<String, String> parameters) {
        this.type = type == null ? WILDCARD_TYPE : type;
        this.subtype = subtype == null ? WILDCARD_TYPE : subtype;
        this.name = type + '/' + subtype;
        this.wildcardType = WILDCARD_TYPE.equals(type);
        this.wildcardSubtype = WILDCARD_TYPE.equals(subtype);

        TreeMap<String, String> params = createParametersMap(parameters);
        if (charset != null && !charset.isEmpty()) {
            params.put(CHARSET_PARAMETER, charset);
        }
        this.parameters = Collections.unmodifiableMap(params);
        
        this.hashcode = (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
    }
    
    public String type() {
        return type;
    }
    
    public String subtype() {
        return subtype;
    }
    
    public Map<String, String> params() {
        return parameters;
    }
    
    public String name() {
        return name;
    }
    
    public boolean isAny() {
        return this.wildcardType && this.wildcardSubtype;
    }
    
    public boolean isWildcardType() {
        return wildcardType;
    }
    
    public boolean isWildcardSubtype() {
        return wildcardSubtype;
    }
    
    public static MediaType valueOf(final String type) throws Err.BadMediaType {
        return parse(type).get(0);
    }
    
    public static List<MediaType> parse(final String type) throws Err.BadMediaType {
        return cache.computeIfAbsent(type, MediaType::_parse);
    }
    
    public boolean matches(final MediaType that) {
        if (this == that || this.wildcardType || that.wildcardType) {
            // same or */*
            return true;
        }
        if (type.equals(that.type)) {
            if (subtype.equals(that.subtype) || this.wildcardSubtype || that.wildcardSubtype) {
                return true;
            }
            if (subtype.startsWith("*+")) {
                return that.subtype.endsWith(subtype.substring(2));
            }
            if (subtype.startsWith(WILDCARD_TYPE)) {
                return that.subtype.endsWith(subtype.substring(1));
            }
        }
        return false;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MediaType) {
            MediaType that = (MediaType) obj;
            return type.equals(that.type) && subtype.equals(that.subtype) && parameters.equals(that.parameters);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return hashcode;
    }
    
    @Override
    public int compareTo(MediaType that) {
        if (this == that) {
            return 0;
        }
        if (this.wildcardType && !that.wildcardType) {
            return 1;
        }

        if (that.wildcardType && !this.wildcardType) {
            return -1;
        }

        if (this.wildcardSubtype && !that.wildcardSubtype) {
            return 1;
        }

        if (that.wildcardSubtype && !this.wildcardSubtype) {
            return -1;
        }

        if (!this.type().equals(that.type())) {
            return 0;
        }

        // parameters size
        int paramsSize1 = this.parameters.size();
        int paramsSize2 = that.parameters.size();
        return (paramsSize2 < paramsSize1 ? -1 : (paramsSize2 == paramsSize1 ? 0 : 1));
    }

    @Override
    public String toString() {
        return name;
    }

    private static TreeMap<String, String> createParametersMap(Map<String, String> initialValues) {
        final TreeMap<String, String> map = new TreeMap<String, String>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        if (initialValues != null) {
            for (Map.Entry<String, String> e : initialValues.entrySet()) {
                map.put(e.getKey().toLowerCase(), e.getValue());
            }
        }
        return map;
    }
    
    private static List<MediaType> _parse(final String value) {
        String[] types = value.split(",");
        List<MediaType> result = new ArrayList<>(types.length);
        for (String type : types) {
            String[] parts = type.trim().split(";");
            if (parts[0].equals(WILDCARD_TYPE)) {
                // odd and ugly media type
                result.add(WILDCARD);
            } else {
                String[] typeAndSubtype = parts[0].split("/");
                if (typeAndSubtype.length != 2) {
                    throw new Err.BadMediaType(value);
                }
                String stype = typeAndSubtype[0].trim();
                String subtype = typeAndSubtype[1].trim();
                if (WILDCARD_TYPE.equals(stype) && !WILDCARD_TYPE.equals(subtype)) {
                    throw new Err.BadMediaType(value);
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
    }



}
