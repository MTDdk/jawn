package implementation.controllers;

import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.Path;

@Path("/test")
public class TestController {
    
    public TestController() {
        System.out.println("initialised");
    }

    @GET
    public void something() {
        System.out.println("something");
    }
}
