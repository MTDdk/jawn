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
     * @param duration "3h" or "4m" or "10s"
     * 
     * @return the number of seconds or 30 minutes (1800) if <code>duration</code> is null or unparseable.
     */
    public static int parse(String duration) {
        if (StringUtil.blank(duration)) return 1800;
        
        
        int lastChar = duration.length()-1;
        char suffix = duration.charAt(lastChar);
        
        try {
            int value = Integer.parseInt(duration.substring(0, lastChar), 10);
        
            if (suffix == 'm') {
                return value * 60;
            } else if (suffix == 's') {
                return value;
            } else if (suffix == 'h') {
                return value * 60 * 60;
            } else if (suffix == 'd') {
                return value *24 * 60 * 60; 
            }
        } catch (NumberFormatException ignore) {}
        
        return 1800;
    }
}
