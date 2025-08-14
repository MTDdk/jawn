package net.javapla.jawn.examples.mvc.controllers;

import com.typesafe.config.Config;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.Inject;
import net.javapla.jawn.core.annotation.PUT;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.annotation.Produces;
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
    
    @PUT
    @Path("/json")
    @Produces(MediaType.json)
    public Object json() {
        record SimpleResponse(String name, int[] list) {}
        return new SimpleResponse("collection", new int[] {12,21,37,73});
    }
}
