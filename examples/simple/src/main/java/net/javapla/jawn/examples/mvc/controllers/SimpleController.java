package net.javapla.jawn.examples.mvc.controllers;

import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.Path;

@Path("/simple")
public class SimpleController {

    public SimpleController() {
        System.out.println(getClass().getSimpleName() + " called multiple time");
    }
    
    @GET
    public String text() {
        return "simple"; 
    }
}
