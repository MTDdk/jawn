package net.javapla.jawn.util;

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
}
