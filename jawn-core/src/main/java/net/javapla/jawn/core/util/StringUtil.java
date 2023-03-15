package net.javapla.jawn.core.util;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class StringUtil {
    private StringUtil() {}

    /**
     * Splits a string into an array using a provided delimiter. The split chunks are also trimmed.
     *
     * @param input string to split.
     * @param delimiter  delimiter
     * @return a string into an array using a provided delimiter
     */
    public static String[] split(String input, char delimiter){
        if (input == null) throw new NullPointerException("input cannot be null");

        final int len = input.length();
        
        // find the number of strings to split into
        int nSplits = 1;
        for (int i = 0; i < len; i++) {
            if (input.charAt(i) == delimiter) {
                nSplits++;
            }
        }

        // do the actual splitting
        String[] result = new String[nSplits];
        int lastMark = 0;
        int lastSplit = 0;
        for (int i = 0; i < len; i++) {
            if (input.charAt(i) == delimiter) {
                result[lastSplit] = input.substring(lastMark, i);
                lastSplit++;
                lastMark = i + 1;// 1 == delimiter length
            }
        }
        result[lastSplit] = input.substring(lastMark, len);

        return result;
    }
    
    /**
     * Splits a string into an array using a provided delimiter.
     * The callback will be invoked once per each found substring
     * 
     * @param input string to split.
     * @param delimiter  delimiter
     * @param callbackPerSubstring called for each splitted string
     */
    public static void split(String input, char delimiter, Consumer<String> callbackPerSubstring) {
        if (input == null) throw new NullPointerException("input cannot be null");
        
        final int len = input.length();
        
        int lastMark = 0;
        for (int i = 1; i < len; i++) {
            if (input.charAt(i) == delimiter) {
                callbackPerSubstring.accept(input.substring(lastMark, i));
                lastMark = i + 1;// 1 == delimiter length
            }
        }
        callbackPerSubstring.accept(input.substring(lastMark, len));
    }
    
    /**
     * split(String[]{"--mode=prod","--performance=medium"}, '=') -> ("--mode","prod"),("--performance","medium")
     * @param lines
     * @param delimiter
     * @param callback
     */
    public static void split(String[] lines, char delimiter, BiConsumer<String,String> callback) {
        if (lines == null || lines.length == 0) return;
        
        for (String line : lines) {
            int del = line.indexOf(delimiter);
            if (del > 0) {
                callback.accept(line.substring(0, del).trim(), line.substring(del + 1).trim());
            } else {
                callback.accept(line.trim(), "");
            }
        }
    }
    
    
    /**
     * Generates a camel case version of a phrase from underscore.
     * "alice_in_wonderland" becomes: "AliceInWonderLand"
     *
     * @param underscored underscored version of a word to converted to camel case.
     * @return camel case version of underscore.
     */
    public static String camelize(String underscored){
        return camelize(underscored, true);
    }


    /**
     * Generates a camel case version of a phrase from underscore.
     * 
     * <pre>
     * if {@code capitalicapitalizeFirstChar}
     *    "alice_in_wonderland" becomes: "AliceInWonderLand"
     * else
     *    "alice_in_wonderland" becomes: "aliceInWonderLand"
     * </pre>
     *
     * @param underscored underscored version of a word to converted to camel case.
     * @param capitalizeFirstChar set to true if first character needs to be capitalized, false if not.
     * @return camel case version of underscore.
     */
    public static String camelize(String underscored, boolean capitalizeFirstChar){
        String result = "";
        StringTokenizer st = new StringTokenizer(underscored, "_");
        while(st.hasMoreTokens()){
            result += capitalize(st.nextToken());
        }        
        return capitalizeFirstChar? result :result.substring(0, 1).toLowerCase() + result.substring(1);            
    }
    
//    /**
//     * Extracts the first part of a camel_cased string.
//     * 
//     * @param camelcased A camel_cased string that may or may not start with an upper cased letter.
//     * @return "get" from "getVideo" or "post" from "postVideoUpload" or "Get" from "GetVideo"
//     */
//    public static String firstPartOfCamelCase(String camelcased) {
//        // by starting to count before the isUpperCase-check,
//        // we do not care if the strings starts with a lower- or uppercase
//        int end = 0;
//        while (++end < camelcased.length()) {
//            if (Character.isUpperCase(camelcased.charAt(end)))
//                break;
//        }
//        return camelcased.substring(0,end);
//    }

    /**
     * Capitalizes a word  - only a first character is converted to upper case.
     * 
     * @param word word/phrase to capitalize.
     * @return same as input argument, but the first character is capitalized.
     */
    public static String capitalize(String word){
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }
    
    /**
     * Decapitalizes a word - only the first character is converted to lower case.
     * 
     * 
     * @param word word/phrase to decapitalize.
     * @return same as input argument, but the first character is in lower case.
     */
    public static String decapitalize(String word) {
        return Character.toLowerCase(word.charAt(0)) + word.substring(1);
    }
    
    /**
     * Converts a CamelCase string to underscores: "AliceInWonderLand" becomes:
     * "alice_in_wonderland"
     *
     * @param camel camel case input
     * @return result converted to underscores.
     */
    public static String underscore(String camel) {

        StringBuilder bob = new StringBuilder(camel); // standard adds 16 extra slots for underscores
        
        // lowercase the first letter
        if (camel.charAt(0) >= 'A' && camel.charAt(0) <= 'Z')
            bob.setCharAt(0, Character.toLowerCase(camel.charAt(0)));
        
        int extra = 0;
        // i = 1, because we already lowered it
        for (int i = 1; i < camel.length(); i++) {
            char b = camel.charAt(i);
            if (b >= 'A' && b <= 'Z') { // within range
                // lower it
                bob.setCharAt(i + extra, Character.toLowerCase(b));
                // add underscore
                bob.insert(i + extra++, '_');
            }
        }

        return bob.toString();
    }
    
    /**
     * Returns true if value is either null or it's String representation is empty.
     *
     * @param value object to check.
     * @return true if value is either null or it's String representation is empty, otherwise returns false.
     */
    public static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    public static String stringOrNull(/*@Nullable */String value) {
        return blank(value) ? null : value;
    }
    
    public static boolean contains(final CharSequence s, final char c) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) return true;
        }
        
        return false;
    }
    
    public static String padEnd(String string, int minLength, char padChar) {
        
        StringBuilder bob = new StringBuilder(minLength);
        bob.append(string);
        for (int i = string.length(); i < minLength; i++) {
            bob.append(padChar);
        }
        
        return bob.toString();
    }
    
    public static boolean startsWith(final CharSequence string, char ... ca) {
        int l = ca.length;
        for (int i = 0; i < l; i++) {
            if (string.charAt(i) != ca[i]) return false;
        }
        return true;
    }
    public static boolean startsWith(final CharSequence string, char ca, char cb) {
        if (string.charAt(0) != ca) return false;
        if (string.charAt(1) != cb) return false;
        return true;
    }
    public static boolean startsWith(final CharSequence string, char ca, char cb, char cc) {
        if (string.charAt(0) != ca) return false;
        if (string.charAt(1) != cb) return false;
        if (string.charAt(2) != cc) return false;
        return true;
    }
    
    public static boolean endsWith(final CharSequence string, char c) {
        return string.charAt(string.length()-1) == c;
    }
    
    /**
     * Sanitizes a URI to conform with the URI standards RFC3986 http://www.ietf.org/rfc/rfc3986.txt.
     * <p>
     * Replaces all the disallowed characters for a correctly formed URI.
     * @param uri The un-sanitized URI
     * @param replace The character to replace the disallowed characters with
     * @return The sanitized input
     */
    public static String sanitizeForUri(String uri, String replace) {
        /*
         * Explanation:
         * [a-zA-Z0-9\\._-] matches a letter from a-z lower or uppercase, numbers, dots, underscores and hyphen
         * [^a-zA-Z0-9\\._-] is the inverse. i.e. all characters which do not match the first expression
         * [^a-zA-Z0-9\\._-]+ is a sequence of characters which do not match the first expression
         * So every sequence of characters which does not consist of characters from a-z, 0-9 or . _ - will be replaced.
         */
        uri = uri.replaceAll("[^a-zA-Z0-9\\._-]+", replace);
        return uri;
    }
    
    // README: To be extracted into some utility or abstract?
    public static String hex(final byte[] bytes) {
        final char[] hexCode = "0123456789abcdef".toCharArray();
        char[] r = new char[bytes.length << 1];
        int index = 0;
        for (byte b : bytes) {
            r[index++] = hexCode[(b >> 4) & 0xF];
            r[index++] = hexCode[(b & 0xF)];
        }
        return new String(r);
    }

    public static final Function<String, String> NOT_BLANK = v -> {
        if (blank(v)) throw new NoSuchElementException();
        return v;
    };
}