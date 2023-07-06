package net.javapla.jawn.examples.mvc.controllers;

import com.typesafe.config.Config;

import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.Inject;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.annotation.Singleton;

@Path("/singleton")
@Singleton
public class SimpleSingletonController {

    @Inject
    public SimpleSingletonController(Config config) {
        System.out.println(getClass().getSimpleName() + " called once");
    }
    
    @GET
    public String text() {
        return "simple singleton";
    }
}
