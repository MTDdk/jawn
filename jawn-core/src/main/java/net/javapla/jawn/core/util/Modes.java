package net.javapla.jawn.core.util;


public enum Modes {

    /** Production: All caching is enabled and everything is trying to run as fast as possible. */
    PROD(Constants.MODE_PRODUCTION), 
    /** Development: No caching, everything is read from disk, so minor changes can be seen instantly. This applies to controllers as well. */
    DEV(Constants.MODE_DEVELOPMENT), 
    /** Testing: Emulates {@link #prod}, but can have a different configuration. For example: a local database in a production environment. */
    TEST(Constants.MODE_TEST);
    
    
    private String mode;

    private Modes(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }
    
}
