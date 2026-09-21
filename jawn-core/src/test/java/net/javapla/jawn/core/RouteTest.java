package net.javapla.jawn.core;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;

class RouteTest {

    @Test
    void handlers() throws Exception {
        Context context = new TestableContext();
        Route.ZeroArgHandler zero = () -> { return 16; };
        Object object = new Route.Builder(HttpMethod.GET, "/", zero).build().handler().handle(context);
        assertEquals(16, object);
        
        
        /*AtomicReference<String> result = new AtomicReference<>("nothing");
        Route.NoResultHandler no = ctx -> { result.set("result"); };
        object = new Route.Builder(HttpMethod.GET, "/", no).build().handler().handle(context);
        assertEquals("result", result.get());
        assertNull(object);*/
        
        
        Route.Handler handler = ctx -> { return 19; };
        object = new Route.Builder(HttpMethod.GET, "/", handler).build().handler().handle(context);
        assertEquals(19, object);
    }
    
    @Test
    void returnType() throws Exception {
        Context context = new TestableContext();
        Object wantedResult = "all good";
        Object object = new Route.Builder(HttpMethod.GET, "/testing", (ctx) -> wantedResult).returnType(wantedResult.getClass()).build().handler().handle(context);
        assertEquals(wantedResult, object);
    }

}
