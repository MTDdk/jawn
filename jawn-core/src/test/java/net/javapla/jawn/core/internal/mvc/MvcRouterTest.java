package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.truth.Correspondence;
import com.google.inject.Injector;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.Path;
import net.javapla.jawn.core.mvc.Produces;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;

public class MvcRouterTest {

    private static final RendererEngineOrchestrator RENDERERS = mock(RendererEngineOrchestrator.class);

    @Test
    public void routeFromMethod() {
        boolean[] called = {false};
        MvcRouter.routes(SingleRoute.class.getDeclaredMethods(), (method, annotations) -> {
            assertThat(method.getName()).isEqualTo("test");
            assertThat(annotations).hasSize(1);
            assertThat(annotations.get(0)).isEqualTo(GET.class);
            called[0] = true;
        });
        assertThat(called[0]).isTrue();
    }
    
    @Test
    public void methodsSimple() {
        boolean[] called = {false};
        MvcRouter.methods(TwoRoutes.class, methods -> {
            assertThat(methods).hasLength(2);
            called[0] = true;
        });
        assertThat(called[0]).isTrue();
    }
    
    @Test
    public void methodsInheritance() {
        int[] i = {0};
        MvcRouter.methods(InheritRoutes.class, methods -> {
            i[0] += methods.length;
        });
        assertThat(i[0]).isEqualTo(3);
    }
    
    @Test
    public void rootPath() {
        assertThat(MvcRouter.paths(SingleRoute.class)).asList().containsExactly("/single");
        assertThat(MvcRouter.paths(TwoRoutes.class)).asList().containsExactly("/two");
        assertThat(MvcRouter.paths(InheritRoutes.class)).asList().containsExactly("/more", "/extra");
        assertThat(MvcRouter.paths(MvcRouterTest.class)).isNull();
    }
    
    @Test
    public void actionPath() {
        MvcRouter.methods(ActionController.class, methods -> {
            assertThat(MvcRouter.mergePaths(MvcRouter.paths(ActionController.class), methods[0])).asList().containsExactly("/controller/action");
        });
        
    }

    @Test
    public void singleRoute() {
        List<Route.BuilderImpl> routes = MvcRouter.extract(SingleRoute.class, mock(ActionParameterProvider.class), mock(Injector.class));
        assertThat(routes).hasSize(1);
        
        Route route = routes.stream().map(bob -> bob.build(RENDERERS)).findFirst().get();
        assertThat(route.path()).isEqualTo("/single");
    }
    
    @Test
    public void multipleRoutes() {
        List<Route.BuilderImpl> builders = MvcRouter.extract(TwoRoutes.class, mock(ActionParameterProvider.class), mock(Injector.class));
        assertThat(builders).hasSize(2);
        
        List<Route> routes = builders.stream().map(bob -> bob.build(RENDERERS)).collect(Collectors.toList());
        
        assertThat(routes)
            .comparingElementsUsing(Correspondence.from( (Route actual, String expected) -> actual.path().equals(expected), "equal"))
            .containsExactly("/two","/two/second");
    }
    
    @Test
    public void emptyControllerPath() {
        List<Route.BuilderImpl> builders = MvcRouter.extract(EmptyControllerPath.class, mock(ActionParameterProvider.class), mock(Injector.class));
        
        Route route = builders.get(0).build(RENDERERS);
        
        // the '/' of the controller should not be simply prepended to the action
        assertThat(route.path()).isNotEqualTo("//image");
    }
    
    @Test
    public void innerLambdas() {
        // When having lambdas within a method, this lambda gets returned by 
        // Method.getDeclaredMethods as "private static java.lang.String net.javapla.jawn.core.internal.mvc.MvcRouterTest$Lambdas.lambda$0(java.lang.Object)"
        
        MvcRouter.methods(Lambdas.class, methods -> {
            assertThat(methods).hasLength(1);
        });
    }
    
    @Test
    public void onlyNonStaticMethods() {
        MvcRouter.methods(StaticMethods.class, methods -> {
            assertThat(methods).hasLength(1);
        });
    }
    
    @Test
    public void producesAnnotation() {
        int[] count = {0};
        MvcRouter.methods(AnnotationsController.class, methods -> {
            for (Method action : methods) {
                MediaType produces = MvcRouter.produces(action);
                assertThat(produces).isEqualTo(MediaType.JSON);
                count[0]++;
            }
        });
        assertThat(count[0]).isEqualTo(1);
    }
    
    @Test
    public void producesAnnotationOnController() {
        List<Route.BuilderImpl> routes = MvcRouter.extract(AnnotationsController2.class, mock(ActionParameterProvider.class), mock(Injector.class));
        assertThat(routes).hasSize(3);
        
        assertThat(routes.get(0).build(RENDERERS).produces()).isEqualTo(MediaType.JSON);
        assertThat(routes.get(1).build(RENDERERS).produces()).isEqualTo(MediaType.JSON);
        assertThat(routes.get(2).build(RENDERERS).produces()).isEqualTo(MediaType.PLAIN);
    }
    
    
/**** TEST CLASSES ****/
    
    @Path("/single")
    static class SingleRoute {
        @GET
        public Result test() {
            return Results.notFound();
        }
    }
    
    @Path("/two")
    static class TwoRoutes {
        @GET
        public void test1() { }
        @GET
        @Path("/second")
        public void test2() { }
    }
    
    @Path("/more")
    @Path("/extra") //TODO create a standalone test for this (@Path-on-@Path)
    static class InheritRoutes extends TwoRoutes {
        @GET
        public void test3() {}
    }
    
    @Path("controller")
    static class ActionController {
        @GET
        @Path("action")
        public void action() {}
    }
    
    @Path("annotations")
    static class AnnotationsController {
        @GET
        @Path("action")
        @Produces(MediaType.json)
        public void action() {}
    }
    
    @Path("annotations")
    @Produces(MediaType.json)
    static class AnnotationsController2 {
        @GET
        @Path("action")
        public void action() {}
        @GET
        @Path("action2")
        public void action2() {}
        @GET
        @Path("action3")
        @Produces("text/plain")
        public void action3() {}
    }
    
    static class Lambdas {
        @GET
        public Result action(Context context) {
            return Results.text(context.attribute("").map(att -> att + "test").orElse("nothing"));
        }
    }
    
    @Path("/")
    static class EmptyControllerPath {
        @Path("/image")
        public void get() {}
    }
    
    static class StaticMethods {
        public void action() {}
        @SuppressWarnings("unused")
        private void privateAction() {}
        void packageAction() {}
        public static void publicstaticAction() {}
        @SuppressWarnings("unused")
        private static void privateStaticAction() {}
        static void packageStaticAction() {}
    }
}
