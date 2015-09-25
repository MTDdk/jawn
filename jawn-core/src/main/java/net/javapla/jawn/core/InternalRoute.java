package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InternalRoute {
    
    protected final static Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");
    /**
     * This regex matches everything in between path slashes.
     */
    final static String VARIABLE_ROUTES_DEFAULT_REGEX = "([^/]*)";

    protected final String uri;
    
    protected final List<String> parameters;
    
    protected final Pattern regex;
    
    public InternalRoute(String uri) {
        this.uri = uri;
        
        parameters = parseParameters(uri);
        
        regex = Pattern.compile(convertRawUriToRegex(uri));
    }
    
    
    /**
     * Matches /index to /index or /person/1 to /person/{id}
     *
     * @return True if the actual route matches a raw route. False if not.
     */
    public boolean matches(String requestUri) {
        Matcher matcher = regex.matcher(requestUri);
        return matcher.matches();
    }
    
    /**
     * This method does not do any decoding / encoding.
     *
     * If you want to decode you have to do it yourself.
     *
     * Most likely with:
     * http://docs.oracle.com/javase/6/docs/api/java/net/URI.html
     *
     * @param requestUri The whole encoded uri.
     * @return A map with all parameters of that uri. Encoded in => encoded out.
     */
    public Map<String, String> getPathParametersEncoded(String requestUri) {
        
        Matcher m = regex.matcher(requestUri);
        
        return mapParametersFromPath(requestUri, parameters, m);
    }
    
    public static Map<String, String> mapPathParameters(String requestUri) {
        List<String> parameters = parseParameters(requestUri);
        
        Pattern regex = Pattern.compile(convertRawUriToRegex(requestUri));
        Matcher m = regex.matcher(requestUri);
        
        return mapParametersFromPath(requestUri, parameters, m);
    }
    private final static Map<String, String> mapParametersFromPath(String requestUri, List<String> parameters, Matcher m) {
        Map<String, String> map = new HashMap<>();
        if (m.matches()) {
            for (int i = 1; i < m.groupCount() + 1; i++) {
                map.put(parameters.get(i - 1), m.group(i));
            }
        }
        return map;
    }
    
    protected static List<String> parseParameters(String uri) {
        List<String> params = new ArrayList<>();
        
        Matcher m = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(uri);
    
        while (m.find()) {
            // group(1) is the name of the group. Must be always there...
            // "/assets/{file}" and "/assets/{file: [a-zA-Z][a-zA-Z_0-9]}" 
            // will return file.
            params.add(m.group(1));
        }
        
        return params;
    }

    /**
     * Gets a raw uri like "/{name}/id/*" and returns "/([^/]*)/id/*."
     *
     * Also handles regular expressions if defined inside routes:
     * For instance "/users/{username: [a-zA-Z][a-zA-Z_0-9]}" becomes
     * "/users/([a-zA-Z][a-zA-Z_0-9])"
     *
     * @return The converted regex with default matching regex - or the regex
     *          specified by the user.
     */
    protected static String convertRawUriToRegex(String rawUri) {

        Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(rawUri);

        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {

            // By convention group 3 is the regex if provided by the user.
            // If it is not provided by the user the group 3 is null.
            String namedVariablePartOfRoute = matcher.group(3);
            String namedVariablePartOfORouteReplacedWithRegex;
            
            if (namedVariablePartOfRoute != null) {
                // we convert that into a regex matcher group itself
                namedVariablePartOfORouteReplacedWithRegex 
                    = "(" + Matcher.quoteReplacement(namedVariablePartOfRoute) + ")";
            } else {
                // we convert that into the default namedVariablePartOfRoute regex group
                namedVariablePartOfORouteReplacedWithRegex 
                    = VARIABLE_ROUTES_DEFAULT_REGEX;
            }
            // we replace the current namedVariablePartOfRoute group
            matcher.appendReplacement(stringBuffer, namedVariablePartOfORouteReplacedWithRegex);

        }

        // .. and we append the tail to complete the stringBuffer
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }
    
    @Override
    public String toString() {
        return uri;
    }
    
    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
