package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.Path;

public class MvcRouterTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}
    
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
        assertThat(MvcRouter.paths(InheritRoutes.class)).asList().containsExactly("/more");
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
        List<Route.Builder> routes = MvcRouter.extract(SingleRoute.class);
        assertThat(routes).hasSize(1);
        
        Route route = routes.stream().map(Route.Builder::build).findFirst().get();
        assertThat(route.path()).isEqualTo("/single");
    }
    
    public void multipleRoutes() {
        List<Route.Builder> builders = MvcRouter.extract(TwoRoutes.class);
        assertThat(builders).hasSize(2);
        
        List<Route> routes = builders.stream().map(Route.Builder::build).collect(Collectors.toList());
        assertThat(routes.get(0).path()).isEqualTo("/two");
        assertThat(routes.get(1).path()).isEqualTo("/two/second");
    }

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
}
