package net.javapla.jawn.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class StringUtil {

    /**
     * Splits a string into an array using a provided delimiter. The split chunks are also trimmed.
     *
     * @param input string to split.
     * @param delimiter  delimiter
     * @return a string into an array using a provided delimiter
     */
    public static String[] split(String input, char delimiter){
        if(input == null) throw new NullPointerException("input cannot be null");

        int len = input.length();
        
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
     * Joins the items in collection with a delimiter.
     *
     * @param collection - collection of items to join.
     * @param delimiter delimiter to insert between elements of collection.
     * @return string with collection elements separated by delimiter. There is no trailing delimiter in the string.
     */
    public static String join(Collection<?> collection, String delimiter){
        if(collection.size() == 0) return "";
        return collection
                    .stream()
                    .filter(o -> o != null)
                    .map(o -> o.toString())
                    .collect(Collectors.joining(delimiter));
    }
    
    /**
     * Joins the items in collection with a delimiter.
     *
     * @param collection - collection of items to join.
     * @param delimiter delimiter to insert between elements of collection.
     * @return string with collection elements separated by delimiter. There is no trailing delimiter in the string.
     */
    public static String join(String delimiter, String... collection){
        if (collection.length == 0) return "";
        
        StringJoiner sj = new StringJoiner(delimiter);
        for (String string : collection) {
            sj.add(string);
        }
        return sj.toString();
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

    /**
     * Capitalizes a word  - only a first character is converted to upper case.
     * 
     * @param word word/phrase to capitalize.
     * @return same as input argument, but the first character is capitalized.
     */
    public static String capitalize(String word){
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
    
    /**
     * Decapitalizes a word - only the first character is converted to lower case.
     * 
     * 
     * @param word word/phrase to decapitalize.
     * @return same as input argument, but the first character is in lower case.
     */
    public static String decapitalize(String word) {
        return word.substring(0, 1).toLowerCase() + word.substring(1);
    }
    
    /**
     * Converts a CamelCase string to underscores: "AliceInWonderLand" becomes:
     * "alice_in_wonderland"
     *
     * @param camel camel case input
     * @return result converted to underscores.
     */
    public static String underscore(String camel) {

        List<Integer> upper = new ArrayList<Integer>();
        byte[] bytes = camel.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b < 97 || b > 122) {
                upper.add(i);
            }
        }

        StringBuffer b = new StringBuffer(camel);
        for (int i = upper.size() - 1; i >= 0; i--) {
            Integer index = upper.get(i);
            if (index != 0)
                b.insert(index, "_");
        }

        return b.toString().toLowerCase();
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
    
    public static final boolean contains(String s, char c) {
        return s.indexOf(c) > -1;
    }
}
