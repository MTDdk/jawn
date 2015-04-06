package net.javapla.jawn.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class MethodNotFoundException extends WebApplicationException {
    private static final long serialVersionUID = 4434876822380907493L;

    public MethodNotFoundException(String methodName) {
        super("MethodNotFoundException " + methodName, Status.NOT_FOUND);
    }
}
