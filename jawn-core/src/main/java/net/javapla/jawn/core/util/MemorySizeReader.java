package net.javapla.jawn.core.util;


public abstract class MemorySizeReader {

    private MemorySizeReader() {}
    
    /**
     * <ul>
     * <li><b>b</b> : bytes</li>
     * <li><b>k</b> : kilobytes</li>
     * <li><b>m</b> : megabytes</li>
     * </ul>
     * @param input "16k" or "16384b" or "12m" 
     * @return
     * @throws IllegalArgumentException
     */
    public static long bytes(String input) throws IllegalArgumentException {
        if (StringUtil.blank(input)) throw new IllegalArgumentException("Input ["+input+"] may not be empty");
        
        /*
         * - Find the next character
         * - Parse the number from beginning of string or last read character to current found character
         * - Repeat as long as the string is not entirely read
         */
        
        int result = 0;
        int lastChar = input.length()-1;
        int suffixChar = 0;
        int firstChar = 0;
        
        do {
            // Find next character
            while (++suffixChar <= lastChar && Character.isDigit(input.charAt(suffixChar)));
            
            char suffix = suffixChar >= input.length() ? 'b' : input.charAt(suffixChar);
            
            long value = bytes(input.substring(firstChar, suffixChar), suffix);
            if (value == -1) throw new IllegalArgumentException("Input ["+input+"] seems invalid");
            
            result += value;
            firstChar = suffixChar +1;
        } while(suffixChar < lastChar);
        
        return result;
    }
    
    public static long bytes(String v, char suffix) {
        try {
            int value = Integer.parseInt(v, 10);
            
            switch (suffix) {
                case 'b': return value;
                case 'k': return value * 1024;
                case 'm': return value * 1024 * 1024;
            }
        } catch (NumberFormatException ignore) {}
        return -1;
    }
}
