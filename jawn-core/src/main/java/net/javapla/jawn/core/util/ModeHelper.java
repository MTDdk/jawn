package net.javapla.jawn.core.util;


public class ModeHelper {

    public static Modes determineModeFromSystem() {
        String ENV = System.getProperty("JAWN_ENV") == null? System.getenv().get("JAWN_ENV"): System.getProperty("JAWN_ENV");
        if(StringUtil.blank(ENV)){                
            return Modes.DEV;
//            LOGGER.warn("Environment variable JAWN_ENV not provided, defaulting to '" + ENV + "'");
        }
        
        try {
            return Modes.valueOf(ENV);
        } catch (IllegalArgumentException e) {
            return determineModeFromString(ENV);
        }
    }
    
    public static Modes determineModeFromString(String mode) {
        for (Modes modes : Modes.values()) {
            if (modes.toString().equals(mode))
                return modes;
        }
        throw new IllegalArgumentException("No enum constant " + Modes.class.getName() + "." + mode);
    }
}
