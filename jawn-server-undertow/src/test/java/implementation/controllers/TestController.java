package implementation.controllers;

import com.google.inject.Inject;

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
    
    /*@GET
    public Result get() {
        Image image2 = image.image(new File("src/test/resources/webapp/img/image.png"));
        System.out.println("get - " + image2);
        return image2.asResult();
    }*/

    @POST
    public void post(Double t) {
        System.out.println("post -" + t);
    }
}