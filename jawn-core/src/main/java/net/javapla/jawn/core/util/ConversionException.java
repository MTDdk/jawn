package net.javapla.jawn.core.util;

public class ConversionException extends RuntimeException {

    private static final long serialVersionUID = 5457757452289804224L;

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
