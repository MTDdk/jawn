package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Plugin.Application;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Registry;
import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.internal.injection.Injector;
import net.javapla.jawn.core.internal.mvc.MvcCompiler;

class E2eMvcTest {

    @Test
    void pathParam() {
        Bootstrapper booter = new Bootstrapper();
        
        List<Builder> builders = MvcCompiler.compile(ControllerParams.class, booter.registry());
        Application application = booter.boot(builders.stream());
        
        
        System.out.println(application.router().retrieve(HttpMethod.GET.ordinal(), "/params/include/123"));
    }
    
    
    @Path("/params")
    private static class ControllerParams {
        
        @GET
        @Path("/include/{id}")
        public void include(String id) { }
    }
}
