package net.javapla.jawn.core.i18n;

public class NotSupportedLanguageException extends RuntimeException {

    private static final long serialVersionUID = 7081798523263088960L;

    public NotSupportedLanguageException() {}
    public NotSupportedLanguageException(String message) {
        super(message);
    }
}
