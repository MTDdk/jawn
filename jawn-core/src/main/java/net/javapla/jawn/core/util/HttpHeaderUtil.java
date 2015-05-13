package net.javapla.jawn.core.util;

import javax.ws.rs.core.MediaType;

public class HttpHeaderUtil {

    /**
     * A http content type should contain a character set like
     * "application/json; charset=utf-8".
     * 
     * If you only want to get "application/json" you can use this method.
     * 
     * See also: http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7.1
     * 
     * @param rawContentType "application/json; charset=utf-8" or "application/json"
     * @return only the contentType without charset. Eg "application/json"
     */
    public static String getContentTypeFromContentTypeAndCharacterSetting(String rawContentType) {
        
        if (rawContentType.contains(";")) {
            return rawContentType.split(";")[0];           
        } else {
            return rawContentType;
        }
        
    }
    
    public static String parseAcceptHeader(String rawHeader) {
        if (rawHeader == null) {
            return MediaType.TEXT_HTML;
        }

        if (rawHeader.indexOf("application/xhtml") != -1
                || rawHeader.indexOf("text/html") != -1
                || rawHeader.startsWith("*/*")) {
            return MediaType.TEXT_HTML;
        }

        if (rawHeader.indexOf("application/xml") != -1
                || rawHeader.indexOf("text/xml") != -1) {
            return MediaType.APPLICATION_XML;
        }

        if (rawHeader.indexOf("application/json") != -1
                || rawHeader.indexOf("text/javascript") != -1) {
            return MediaType.APPLICATION_JSON;
        }

        if (rawHeader.indexOf("text/plain") != -1) {
            return MediaType.TEXT_PLAIN;
        }

        if (rawHeader.indexOf("application/octet-stream") != -1) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        if (rawHeader.endsWith("*/*")) {
            return MediaType.TEXT_HTML;
        }

        return MediaType.TEXT_HTML;
    }
}
