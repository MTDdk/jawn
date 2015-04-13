package net.javapla.jawn.i18n;

public class LanguagesNotSetException extends RuntimeException {

    private static final long serialVersionUID = 2997970527439901005L;

    public LanguagesNotSetException() {}
    public LanguagesNotSetException(String message) {
        super(message);
    }
}
