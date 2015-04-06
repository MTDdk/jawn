package net.javapla.jawn.exceptions;

public class ParsableException extends WebException {

    private static final long serialVersionUID = -8906042570057264570L;

    public ParsableException(String m) {
        super(m);
    }
    
    public ParsableException(Throwable t) {
        super(t);
    }
    
    public ParsableException(Class<?> clazz) {
        super("Could not parse class: " + clazz.getName());
    }
}
