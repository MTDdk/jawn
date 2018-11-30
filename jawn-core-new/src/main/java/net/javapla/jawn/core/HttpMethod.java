package net.javapla.jawn.core;

import java.util.Arrays;
import java.util.function.Supplier;

import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.StringUtil;


/**
 * @author MTD
 */
public enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS/*, PATCH, CONNECT, TRACE*/;
    
    public static String[] listHttpMethods() {
        return Arrays.stream(HttpMethod.values()).map(method -> method.name().toLowerCase()).toArray(String[]::new);
    }

    /**
     * Detects an HTTP method from a request.
     */
    public static HttpMethod getMethod(Context context) {
        return _getMethod(context.method(), () -> context.getParameter("_method"));
    }
    
    public static HttpMethod getMethod(String requestMethod, MultiList<String> params) {
        return _getMethod(requestMethod, () -> params.first("_method"));
    }
    
    // Using a supplier in order to postpone calculation to later if necessary
    private static HttpMethod _getMethod(String requestMethod, Supplier<String> _method) {
        if (requestMethod.charAt(0) == 'G') {
            return HttpMethod.GET;
        }
        
        // Sometimes an ajax request can only be sent as GET or POST.
        // We can emulate PUT and DELETE by sending a parameter '_method=PUT' or '_method=DELETE'.
        // Under the assumption that a request method always is sent in upper case
        if (StringUtil.startsWith(requestMethod, 'P','O')) {
            String methodParam = _method.get();
            requestMethod = methodParam != null && methodParam.equalsIgnoreCase("DELETE")? "DELETE" : requestMethod;
            requestMethod = methodParam != null && methodParam.equalsIgnoreCase("PUT")? "PUT" : requestMethod;
        }
        
        return HttpMethod.valueOf(requestMethod.toUpperCase());
    }
}
