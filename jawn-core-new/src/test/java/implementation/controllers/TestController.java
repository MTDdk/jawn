package implementation.controllers;

import com.google.inject.Inject;

import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.mvc.Body;
import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.POST;
import net.javapla.jawn.core.mvc.Path;
import net.javapla.jawn.core.mvc.ViewController;
import net.javapla.jawn.core.util.Modes;

@Path("/test")
@Path("/kage")
@ViewController
public class TestController {
    
    @Inject
    public TestController(Modes mode) {
        System.out.println("initialised + " + mode);
    }

    @GET
    public View something() {
        System.out.println("something");
        return Results.view();
    }
    
    @POST
    public void post(Double t) {
        System.out.println("post -" + t);
    }
}
