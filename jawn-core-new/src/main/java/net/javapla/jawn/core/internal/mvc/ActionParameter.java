package net.javapla.jawn.core.internal.mvc;

import java.lang.reflect.Parameter;

// parameter for a request
public class ActionParameter {

    private final Parameter parameter;
    private final String name;

    public ActionParameter(final Parameter parameter, final String name) {
        this.parameter = parameter;
        this.name = name;
        
    }
    
}
