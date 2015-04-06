package net.javapla.jawn;

/**
 * Conveniently wraps status codes into simple method calls
 * 
 * @author MTD
 */
public class StatusWrapper {
//    private final HttpSupport.HttpBuilder builder;
//    
//    
//    StatusWrapper(HttpSupport.HttpBuilder builder) {
//        this.builder = builder;
//    }
    
    private final NewControllerResponseBuilder builder;
    StatusWrapper(NewControllerResponseBuilder builder) {
        this.builder = builder;
    }
    
    
    /**
     * 200 OK
     * @return 
     * @return The original builder
     */
    public NewControllerResponseBuilder ok() {
//        this.builder.controllerResponse.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode());
//        return builder;
        builder.status(javax.ws.rs.core.Response.Status.OK.getStatusCode());
        return builder;
    }
    /**
     * 204 No Content
     * @return The original builder
     */
    public NewControllerResponseBuilder noContent() {
        builder.status(javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode());
        return builder;
    }
    /**
     * 400 Bad Request
     * @return The original builder
     */
    public NewControllerResponseBuilder badRequest() {
        builder.status(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode());
        return builder;
    }
    /**
     * 404 Not Found
     * @return The original builder
     */
    public NewControllerResponseBuilder notFound() {
        builder.status(javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode());
        return builder;
    }
    /**
     * 500 Internal Server Error
     * @return The original builder
     */
    public NewControllerResponseBuilder internalServerError() {
        builder.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return builder;
    }
}