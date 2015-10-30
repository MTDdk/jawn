package net.javapla.jawn.core.http;

import net.javapla.jawn.core.util.StringUtil;


/**
 * @author Igor Polevoy
 * @author MTD
 */
public enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD;

    /**
     * Detects an HTTP method from a request.
     */
    public static HttpMethod getMethod(Context context) {
        String requestMethod = context.method();
        
        if (requestMethod.charAt(0) == 'G') {
            return HttpMethod.GET;
        }
        
        // under the assumption that a request method always is sent in upper case
        if (StringUtil.startsWith(requestMethod, 'P','O')) {
            String methodParam = context.getParameter("_method");
            
            requestMethod = /*requestMethod.equalsIgnoreCase("POST") &&*/ methodParam != null && methodParam.equalsIgnoreCase("DELETE")? "DELETE" : requestMethod;
            requestMethod = /*requestMethod.equalsIgnoreCase("POST") &&*/ methodParam != null && methodParam.equalsIgnoreCase("PUT")? "PUT" : requestMethod;
        }
        
        return HttpMethod.valueOf(requestMethod.toUpperCase());
    }
    
}
