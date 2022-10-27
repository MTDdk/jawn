package net.javapla.jawn.core;

// Exception
public /*abstract*/ class Up extends RuntimeException {

    private static final long serialVersionUID = 4840075238541075867L;
    
    private final Status status;
    
    public Up(Status status, Throwable cause) {
        super(message(status, null), cause);
        this.status = status;
    }
    public Up(Status status, String msg) {
        super(message(status, msg));
        this.status = status;
    }
    
    public Status status() {
        return status;
    }
    
    /*public abstract Up because();*/

    public static Up.IO IO(Throwable cause) {
        return Up.IO.because(cause);
    }
    public static class IO extends Up {
        private static final long serialVersionUID = -4272247081350518486L;
        public IO(Throwable cause) {
            super(Status.SERVER_ERROR, cause);
        }
        public static IO because(Throwable cause) {
            return new IO(cause);
        }
    }
    
    public static Up RouteMissing(String path) {
        return new Up(Status.NOT_FOUND, path);
    }
    
    /**
     * Build an error message using the HTTP status.
     *
     * @param status The HTTP Status.
     * @param tail A message to append.
     * @return An error message.
     */
    private static String message(final Status status, final String tail) {
        return message(status.reason(), status.value(), tail);
    }

    /**
     * Build an error message using the HTTP status.
     *
     * @param reason Reason.
     * @param status The Status.
     * @param tail A message to append.
     * @return An error message.
     */
    private static String message(final String reason, final int status, final String tail) {
        return reason + "(" + status + ")" + (tail == null ? "" : ": " + tail);
    }
    
    public static boolean isFatal(Throwable x) {
        return x instanceof InterruptedException
            || x instanceof LinkageError
            || x instanceof ThreadDeath
            || x instanceof VirtualMachineError;
    }
}
