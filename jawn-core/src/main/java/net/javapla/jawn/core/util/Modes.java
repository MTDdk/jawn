package net.javapla.jawn.core.util;


public enum Modes {

    /** Production: All caching is enabled and everything is trying to run as fast as possible. */
    PROD(Constants.MODE_PRODUCTION), 
    /** Development: No caching, everything is read from disk, so minor changes can be seen instantly. This applies to controllers as well. */
    DEV(Constants.MODE_DEVELOPMENT), 
    /** Testing: Emulates {@link #prod}, but can have a different configuration. For example: a local database in a production environment. */
    TEST(Constants.MODE_TEST);
    
    
    private final String mode;

    private Modes(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }
    
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
    
    public static Modes determineModeFromString(final String mode) {
        final String m = mode.toLowerCase();
        for (Modes modes : Modes.values()) {
            if (modes.toString().equals(m) || modes.name().toLowerCase().equals(m))
                return modes;
        }
        throw new IllegalArgumentException("No enum constant " + Modes.class.getName() + "." + mode);
    }
    
}
