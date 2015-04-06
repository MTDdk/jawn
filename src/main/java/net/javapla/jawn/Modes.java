package net.javapla.jawn;

import net.javapla.jawn.util.Constants;

public enum Modes {

    prod(Constants.MODE_PRODUCTION), 
    dev(Constants.MODE_DEVELOPMENT), 
    test(Constants.MODE_TEST);
    
    
    private String mode;

    private Modes(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }
    
}
