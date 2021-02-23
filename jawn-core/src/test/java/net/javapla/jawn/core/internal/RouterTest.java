package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Up;

public class RouterTest {

    @Test
    public void simple() {
        List<Route> routes = Arrays.asList(new Route.Builder(HttpMethod.GET).path("/first").build());
        
        Router router = new Router(routes);
        
        Route route = router.retrieve(HttpMethod.GET, "/first");
        assertThat(route).isNotNull();
    }
    
    @Test
    @Ignore("This ought to be handled by the HttpHandlerImpl")
    public void endingSlash_not_needed() {
        List<Route> routes = Arrays.asList(
            new Route.Builder(HttpMethod.GET).path("/first").build(),
            new Route.Builder(HttpMethod.GET).path("/first/second").build(),
            new Route.Builder(HttpMethod.GET).path("/").build()
        );
        
        Router router = new Router(routes);
        
        Route route = router.retrieve(HttpMethod.GET, "/first/");
        assertThat(route).isNotNull();
        route = router.retrieve(HttpMethod.GET, "/first/second/");
        assertThat(route).isNotNull();
        route = router.retrieve(HttpMethod.GET, "/");
        assertThat(route).isNotNull();
    }
    
    @Test
    public void samePathNotSameMethod() {
        List<Route> routes = Arrays.asList(new Route.Builder(HttpMethod.GET).path("/first").build());
        
        Router router = new Router(routes);
        
        try {
            router.retrieve(HttpMethod.POST, "/first");
            fail();
        } catch (Up.RouteFoundWithDifferentMethod e) {}
        
        try {
            router.retrieve(HttpMethod.PUT, "/first");
            fail();
        } catch (Up.RouteFoundWithDifferentMethod e) {}
        
        try {
            router.retrieve(HttpMethod.DELETE, "/first");
            fail();
        } catch (Up.RouteFoundWithDifferentMethod e) {}
    }
    
    @Test
    public void sameComplexPathNotSameMethod() {
        List<Route> routes = Arrays.asList(
            new Route.Builder(HttpMethod.GET).path("/v1/first/more/{id}").build(), 
            new Route.Builder(HttpMethod.POST).path("/v1/first/more/{id}").build()
        );
        
        Router router = new Router(routes);
        
        Route route = router.retrieve(HttpMethod.POST, "/v1/first/more/73"); // should NOT throw
        assertThat(route).isNotNull();
        
        route = router.retrieve(HttpMethod.GET, "/v1/first/more/73"); // should NOT throw
        assertThat(route).isNotNull();
        
        try {
            router.retrieve(HttpMethod.DELETE, "/v1/first/more/67");
            fail();
        } catch (Up.RouteFoundWithDifferentMethod e) {}
    }
    
     @Test
     public void headShouldAlwaysWork() {
         List<Route> routes = Arrays.asList(
             new Route.Builder(HttpMethod.GET).path("/first").build(),
             new Route.Builder(HttpMethod.DELETE).path("/delete").build()
         );
         
         Router router = new Router(routes);
         
         Route route = router.retrieve(HttpMethod.GET, "/first");
         assertThat(route).isNotNull();
         
         route = router.retrieve(HttpMethod.HEAD, "/first");
         assertThat(route).isNotNull();
         
         route = router.retrieve(HttpMethod.HEAD, "/delete");
         assertThat(route).isNotNull();
     }
     
     @Test
     public void headShouldAlwaysWork_alsoWithComplexPaths() {
         List<Route> routes = Arrays.asList(
             new Route.Builder(HttpMethod.GET).path("/first/complex/{id}").build(),
             new Route.Builder(HttpMethod.DELETE).path("/delete/complex/{id}").build()
         );
         
         Router router = new Router(routes);
         
         Route route = router.retrieve(HttpMethod.GET, "/first/complex/73");
         assertThat(route).isNotNull();
         
         route = router.retrieve(HttpMethod.HEAD, "/first/complex/11");
         assertThat(route).isNotNull();
         
         route = router.retrieve(HttpMethod.HEAD, "/delete/complex/13");
         assertThat(route).isNotNull();
     }
     
     @Test
     public void wildcards() {
         List<Route> routes = Arrays.asList(
             new Route.Builder(HttpMethod.GET).path("/first/{something}").build(),
             new Route.Builder(HttpMethod.GET).path("/second/{something}/more").build()
         );
         
         Router router = new Router(routes);
         
         Route route = router.retrieve(HttpMethod.GET, "/first/cookie");
         assertThat(route).isNotNull();
         
         route = router.retrieve(HttpMethod.GET, "/second/cookie/more");
         assertThat(route).isNotNull();
     }

     @Test
     public void routeWithCorrectMethod() {
         List<Route> routes = Arrays.asList(
             new Route.Builder(HttpMethod.GET).path("/first/{some}").build(),
             new Route.Builder(HttpMethod.DELETE).path("/delete").build(),
             new Route.Builder(HttpMethod.POST).path("/post/{test}").build()
         );
         
         Router router = new Router(routes);
         
         try {
             router.retrieve(HttpMethod.POST, "/first/some");
             fail();
         } catch (Up.RouteFoundWithDifferentMethod e) {}
         
         try {
             router.retrieve(HttpMethod.DELETE, "/first");
             fail();
         } catch (Up.RouteMissing e) {}
         
         Route route = router.retrieve(HttpMethod.POST, "/post/concrete");
         assertThat(route.method()).isEqualTo(HttpMethod.POST);
         
         route = router.retrieve(HttpMethod.DELETE, "/delete");
         assertThat(route.method()).isEqualTo(HttpMethod.DELETE);
     }
     
     @Test
     public void resemblingRoutes() {
         // a custom route might *look* like an asset route and get picked before the asset
         
         List<Route> routes = Arrays.asList(
             new Route.Builder(HttpMethod.GET).path("/legit/site").build(),
             new Route.Builder(HttpMethod.GET).path("/{site}/{slug}").build(), // this might override /css/{file} if found first
             new Route.Builder(HttpMethod.GET).path("/css/{file: .*}").build(),
             new Route.Builder(HttpMethod.GET).path("/js/{file: .*}").build()
         );
         
         
         Router router = new Router(routes);
         
         Route css = router.retrieve(HttpMethod.GET, "/css/style.css"); // should definitely return /css/{file}
         assertThat(css.path()).isEqualTo("/css/{file: .*}");
         
         Route js = router.retrieve(HttpMethod.GET, "/js/script.js"); // should definitely return /js/{file}
         assertThat(js.path()).isEqualTo("/js/{file: .*}");
         
         // just to be sure
         assertThat(router.retrieve(HttpMethod.GET, "/legit/site").path()).isEqualTo("/legit/site");
     }
     
     @Test(expected = Up.RouteAlreadyExists.class)
     public void notifyWhenRoutesSeemSimilar() {
         List<Route> routes = Arrays.asList(
             
             // these two are close, but not similar
             new Route.Builder(HttpMethod.GET).path("/css/{file: .*}").build(),
             new Route.Builder(HttpMethod.GET).path("/{site}/{slug: [a-z]}").build(),
             
             // this should break
             new Route.Builder(HttpMethod.GET).path("/{notsite}/{definitelynotslug: [0-9]+}").build()
         );
         
         new Router(routes);
     }
}
