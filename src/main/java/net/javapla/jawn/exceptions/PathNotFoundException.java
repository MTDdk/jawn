package net.javapla.jawn.exceptions;

/**
 * 
 * @author MTD
 */
public class PathNotFoundException extends WebException {

    private static final long serialVersionUID = -4451283815022279103L;

    public PathNotFoundException(String path) {
        super(path);
    }
    
    public PathNotFoundException(Throwable t) {
        super(t);
    }
}
