package net.javapla.jawn.core.templates;

import java.util.Optional;

import javax.ws.rs.core.MediaType;

public class ContentType {
    
    public static enum MediaTypes {
        
        APPLICATION_ATOM_XML(MediaType.APPLICATION_ATOM_XML_TYPE),
        APPLICATION_FORM_URLENCODED(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
        APPLICATION_JSON(MediaType.APPLICATION_JSON_TYPE),
        APPLICATION_OCTET_STREAM(MediaType.APPLICATION_OCTET_STREAM_TYPE),
        APPLICATION_SVG_XML(MediaType.APPLICATION_SVG_XML_TYPE),
        APPLICATION_XHTML_XML(MediaType.APPLICATION_XHTML_XML_TYPE),
        APPLICATION_XML(MediaType.APPLICATION_XML_TYPE),
        MULTIPART_FORM_DATA(MediaType.MULTIPART_FORM_DATA_TYPE),
        TEXT_HTML(MediaType.TEXT_HTML_TYPE),
        TEXT_PLAIN(MediaType.TEXT_PLAIN_TYPE),
        TEXT_XML(MediaType.TEXT_XML_TYPE),
        WILDCARD(MediaType.WILDCARD_TYPE),
        CUSTOM(null)
        ;
        
        final MediaType type;
        MediaTypes(MediaType mediaType) {
            type = mediaType;
        }
        public MediaType getMediaType() {
            return type;
        }
    }
    
    public static final ContentType APPLICATION_ATOM_XML = ContentType.of(MediaType.APPLICATION_ATOM_XML_TYPE);
    public static final ContentType APPLICATION_FORM_URLENCODED = ContentType.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    public static final ContentType APPLICATION_JSON = ContentType.of(MediaType.APPLICATION_JSON_TYPE);
    public static final ContentType APPLICATION_OCTET_STREAM = ContentType.of(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    public static final ContentType APPLICATION_SVG_XML = ContentType.of(MediaType.APPLICATION_SVG_XML_TYPE);
    public static final ContentType APPLICATION_XHTML_XML = ContentType.of(MediaType.APPLICATION_XHTML_XML_TYPE);
    public static final ContentType APPLICATION_XML = ContentType.of(MediaType.APPLICATION_XML_TYPE);
    public static final ContentType MULTIPART_FORM_DATA = ContentType.of(MediaType.MULTIPART_FORM_DATA_TYPE);
    public static final ContentType TEXT_HTML = ContentType.of(MediaType.TEXT_HTML_TYPE);
    public static final ContentType TEXT_PLAIN = ContentType.of(MediaType.TEXT_PLAIN_TYPE);
    public static final ContentType TEXT_XML = ContentType.of(MediaType.TEXT_XML_TYPE);
    public static final ContentType WILDCARD = ContentType.of(MediaType.WILDCARD_TYPE);
    
    private final String type;
    private final String subType;
    //private final String charset;
    private Optional<Integer> sequence;
    
    public ContentType(MediaType type) {
        this(type.getType(),type.getSubtype());
    }
    
    public ContentType(String type, String subType) {
        this.type = type;
        this.subType = subType;
        sequence = Optional.empty();
    }
    
    public static ContentType of(MediaType mediaType) {
        return new ContentType(mediaType);
    }
    
    Optional<Integer> mediaTypeSequence() {
        return sequence;
    }
    void mediaTypeSequence(int sequenceOrder) {
        sequence = Optional.of(sequenceOrder);
    }
} 
