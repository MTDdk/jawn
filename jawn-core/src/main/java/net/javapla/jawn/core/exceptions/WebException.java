package net.javapla.jawn.core.exceptions;


/**
 * @author Igor Polevoy
 * @author MTD
 */
public class WebException extends RuntimeException {
    private static final long serialVersionUID = 8102288824861312266L;
    
    public static final int DEFAULT_CODE = 500;
    
    protected final int http_code;

    public WebException() {
        this(DEFAULT_CODE);
    }
    public WebException(int http_code) {
        super();
        this.http_code = http_code;
    }

    public WebException(String message) {
        this(message, DEFAULT_CODE);
    }
    public WebException(String message, int http_code) {
        super(message);
        this.http_code = http_code;
    }

    public WebException(String message, Throwable cause) {
        this(message, cause, DEFAULT_CODE);
    }
    public WebException(String message, Throwable cause, int http_code) {
        super(message, cause);
        this.http_code = http_code;
    }

    public WebException(Throwable cause) {
        this(cause, DEFAULT_CODE);
    }
    public WebException(Throwable cause, int http_code) {
        super(cause);
        this.http_code = http_code;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if(getCause() != null){
            message += "; " + getCause().getMessage(); 
        }
        return message;
    }
    
    public int getHttpCode() {
        return http_code;
    }
}
