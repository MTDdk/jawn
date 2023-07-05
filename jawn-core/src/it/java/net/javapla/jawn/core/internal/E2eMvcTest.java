package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Plugin.Application;
import net.javapla.jawn.core.AbstractContext;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.Router.RoutePath;
import net.javapla.jawn.core.TestableContext;
import net.javapla.jawn.core.annotation.GET;
import net.javapla.jawn.core.annotation.Path;
import net.javapla.jawn.core.annotation.PathParam;
import net.javapla.jawn.core.internal.mvc.MvcCompiler;

class E2eMvcTest {
    
    private static Application application;

    @BeforeAll
    static void beforeAll() {
        Bootstrapper booter = new Bootstrapper();
        
        List<Builder> builders = MvcCompiler.compile(ControllerParams.class, booter.registry());
        application = booter.boot(builders.stream());
    }
    
    
    
    @Test
    void pathParam() {
        RoutePath route = application.router().retrieve(HttpMethod.GET.ordinal(), "/params/include/1238");
        System.out.println(route);
        assertEquals("1238",route.pathParameters.get("id"));
        
        
        AbstractContext context = new TestableContext();
        route.execute(context);
        assertEquals("1238", ControllerParams.called);
    }
    
    @Test
    void contextType() {
        RoutePath route = application.router().retrieve(HttpMethod.GET.ordinal(), "/params/context");
        System.out.println(route);
        
        AbstractContext context = new TestableContext();
        route.execute(context);
        assertEquals("contextaction", ControllerParams.called);
    }
    
    @Test
    void requestType() {
        RoutePath route = application.router().retrieve(HttpMethod.GET.ordinal(), "/params/request");
        System.out.println(route);
        
        AbstractContext context = new TestableContext();
        route.execute(context);
        assertEquals("requestaction", ControllerParams.called);
    }
    
    
/* 
 * ************
 * TEST CLASSES
 * ************
 */
    
    @Path("/params")
    public static class ControllerParams {
        
        static String called;
        
        @GET
        @Path("/include/{id}")
        public void include(@PathParam String id) { called = id; }
        
        @GET
        @Path("/context")
        public void context(Context ctx) { called = "contextaction"; }
        
        @Path("/request")
        public void request(Context.Request req) { called = "requestaction"; }
    }
}
