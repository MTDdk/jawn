package net.javapla.jawn.core.util;

public class TimeUtil {

    /**
     * Parses a duration to seconds.
     * Valid suffixes are:
     * <ul>
     * <li><b>d</b> : days</li>
     * <li><b>h</b> : hours</li>
     * <li><b>m</b> : minutes</li>
     * <li><b>s</b> : seconds</li>
     * </ul>
     * 
     * @param duration "3h" or "4m" or "10s" or a mixture "4m2s"
     * 
     * @return the number of seconds or 30 minutes (1800) if <code>duration</code> is null or unparseable.
     */
    public static int parse(String duration) {
        if (StringUtil.blank(duration)) return 1800;
        
        /*
         * - Find the next character
         * - Parse the number from beginning of string or last read character to current found character
         * - Repeat as long as the string is not entirely read
         */
        
        int result = 0;
        int lastChar = duration.length()-1;
        int suffixChar = 0;
        int firstChar = 0;
        
        do {
            // Find next character
            while (++suffixChar <= lastChar && Character.isDigit(duration.charAt(suffixChar)));
            
            int value = value(duration.substring(firstChar, suffixChar), duration.charAt(suffixChar));
            if (value == -1) return 1800;
            
            result += value;
            firstChar = suffixChar +1;
        } while(suffixChar < lastChar);
        
        return result;
    }
    
    public static int value(String v, char suffix) {
        try {
            int value = Integer.parseInt(v, 10);
            
            switch (suffix) {
                case 'd': return value * 24 * 60 * 60;
                case 'h': return value * 60 * 60;
                case 'm': return value * 60;
                case 's': return value;
            }
        } catch (NumberFormatException ignore) {}
        return -1;
    }
    
}
