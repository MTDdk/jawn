package net.javapla.jawn.core;

/**
 * 
 * @author MTD
 */
public enum HttpMethod {

    GET(false),
    HEAD(false),
    DELETE(false),
    OPTIONS(false),
    POST(true),
    PUT(true);
    
    /**
     * Not all requests have one: requests fetching resources, 
     * like GET, HEAD, DELETE, or OPTIONS, usually don't need one. 
     * Some requests send data to the server in order to update it: 
     * as often the case with POST requests (containing HTML form data).
     */
    public final boolean mightContainBody;
    //public final int hash;
    
    private HttpMethod(boolean mightContainBody) {
        this.mightContainBody = mightContainBody;
        //this.hash = name().hashCode();
    }
    
    
    public static final String AJAX_METHOD_PARAMETER = "_method";
    
    
    // Using a supplier in order to postpone calculation to later if necessary
    public static HttpMethod _getMethod(final CharSequence requestMethod/*, Supplier<MultiList<FormItem>> formdata*/) {
        char first = requestMethod.charAt(0);
        switch (first) {
            case 'G':
                return HttpMethod.GET;
            case 'D':
                return HttpMethod.DELETE;
            case 'H':
                return HttpMethod.HEAD;
            case 'O':
                return HttpMethod.OPTIONS;
            case 'P':
                if (requestMethod.charAt(1) == 'U') return HttpMethod.PUT;
                
                // assume POST
                
                // Sometimes an ajax request can only be sent as GET or POST.
                // We can emulate PUT and DELETE by sending a parameter '_method=PUT' or '_method=DELETE'.
                // Under the assumption that a request method always is sent in upper case
                /*final CharSequence methodParam = formdata.get().firstOptionally(AJAX_METHOD_PARAMETER).map(Context.FormItem::value).map(Optional::get).orElse(null);
                if (methodParam != null) {
                    // assume DELETE
                    if (StringUtil.startsWith(methodParam, 'D', 'E', 'L')) return HttpMethod.DELETE;
                    // PUT
                    if (StringUtil.startsWith(methodParam, 'P', 'U', 'T')) return HttpMethod.PUT;
                }*/
                return HttpMethod.POST;
        }
        
        throw new IllegalArgumentException();
    }
    
    public static HttpMethod _getMethod(byte[] bytes) {
        switch(bytes[0]) {
            case 'G':
                return HttpMethod.GET;
            case 'D':
                return HttpMethod.DELETE;
            case 'H':
                return HttpMethod.HEAD;
            case 'O':
                return HttpMethod.OPTIONS;
            case 'P':
                if (bytes[1] == 'U') return HttpMethod.PUT;
                return HttpMethod.POST;
        }
        throw new IllegalArgumentException();
    }
    
    public static HttpMethod _getMethod(ByteArray ba) {
        switch(ba.byteAt(0)) {
            case 'G':
                return HttpMethod.GET;
            case 'D':
                return HttpMethod.DELETE;
            case 'H':
                return HttpMethod.HEAD;
            case 'O':
                return HttpMethod.OPTIONS;
            case 'P':
                if (ba.byteAt(1) == 'U') return HttpMethod.PUT;
                return HttpMethod.POST;
        }
        throw new IllegalArgumentException();
    }
    
    @FunctionalInterface
    public static interface ByteArray {
        byte byteAt(int index);
    }
}
