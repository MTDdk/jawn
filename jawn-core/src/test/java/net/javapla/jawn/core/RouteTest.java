package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import net.javapla.jawn.core.Context.Response;
import net.javapla.jawn.core.Route.After;
import net.javapla.jawn.core.Route.Before;
import net.javapla.jawn.core.Route.Handler;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;

public class RouteTest {
    
    /*@Test
    public void beforeSimpleResultOverride() {
        AtomicBoolean called = new AtomicBoolean(false);
        Context context = mock(Context.class);
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler((c) -> {called.set(true); return Results.noContent();})
            .before((c,ch) -> Results.ok())
            .build();
        
        
        // execute
        Result result = handler.handle(context);
        
        assertThat(called.get()).isFalse();
        assertThat(result.status()).isNotNull();
        assertThat(result.status().value()).isEqualTo(200);
    }
    
    @Test
    public void afterSimpleResultOverride() {
        AtomicBoolean called = new AtomicBoolean(false);
        Context context = mock(Context.class);
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler((c) -> {called.set(true); return Results.noContent();})
            .after((c,r) -> Results.ok())
            .build();
        
        // execute
        Result result = handler.handle(context);
        
        // assert
        assertThat(called.get()).isTrue();
        assertThat(result.status()).isNotNull();
        assertThat(result.status().value()).isEqualTo(200);
    }

    @Test
    public void after_should_alwayBeExecuted() {
        boolean[] executed = new boolean[3];
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler((c) -> { executed[0]=true; return Results.noContent(); }) //  not called, because before#1
            .before((c,ch) -> Results.ok()) // before#1
            .before((c,ch) -> { executed[2]=true; return Results.notFound(); }) // not called, because before#1 returns a result instead of calling the chain 
            .after((c,r) -> {executed[1] = true; return r; })
            .build();
        
        
        Context context = mock(Context.class);
        
        Result result = handler.handle(context);
        
        assertThat(executed[0]).isFalse();
        assertThat(executed[1]).isTrue();
        assertThat(executed[2]).isFalse();
        assertThat(result.status()).isEqualTo(Status.OK);
    }*/
    
    @Test
    public void executionOrder() {
        Before before = mock(Route.Before.class);
        when(before.then(any(Route.Handler.class))).thenCallRealMethod();
        doNothing().when(before).before(any(Context.class));
        
        Handler handler = mock(Route.Handler.class);
        when(handler.then(any(Route.After.class))).thenCallRealMethod();
        when(handler.handle(any(Context.class))).thenAnswer(ctx -> {/*System.out.println("handler");*/ return Results.ok();});
        
        
        After after = mock(Route.After.class);
        when(after.then(any(Route.After.class))).thenCallRealMethod();
        //doAnswer(AdditionalAnswers.answerVoid((Context c, Object result) -> {System.out.println("after");})).when(after).after(any(Context.class),any(Object.class));
        doNothing().when(after).after(any(Context.class),any(Object.class));
        
        Response response = mock(Context.Response.class);
        when(response.committed()).thenReturn(false);
        
        Context context = mock(Context.class);
        when(context.resp()).thenReturn(response);
        
        
        // execute
        Route.BuilderImpl bob = (Route.BuilderImpl)new Route.BuilderImpl(HttpMethod.GET, "/", handler)
            .before(before)
            .after(after);
        Route route = bob.build(mock(RendererEngineOrchestrator.class));
        /*Object result = */route.handle(context);

        
        // verify
        InOrder inOrder = Mockito.inOrder(before, handler, after);
        inOrder.verify(before).before(any(Context.class));
        inOrder.verify(handler).handle(any(Context.class));
        inOrder.verify(after).after(any(Context.class), any(Object.class));
    }

    /*@Test
    public void throw_when_afterReturnsNull() {
        Route.BuilderImpl bob = (BuilderImpl) new Route.BuilderImpl(HttpMethod.GET, "/", (c) -> Results.ok()).after((c,r) -> (Result) null);
        Route handler = bob.build(mock(RendererEngineOrchestrator.class));
        
        Context context = mock(Context.class);
        
        assertThrows(Up.BadResult.class, () -> handler.handle(context));
    }*/
    
    @Test
    @Ignore("Handlers are allowed to return null now")
    public void throw_when_handlerReturnsNull() {
        Route handler = new Route.BuilderImpl(HttpMethod.GET, "/", c -> (Result) null)
            .build(mock(RendererEngineOrchestrator.class));
        
        Context context = mock(Context.class);
        
        assertThrows(Up.BadResult.class, () -> handler.handle(context));
    }

    @Test
    public void pathWithVariable_shouldNot_matchRoot() {
        Route route = new Route.BuilderImpl(HttpMethod.GET, "/{name}", Route.NOT_FOUND).build(mock(RendererEngineOrchestrator.class));
        assertThat(route.matches("/")).isFalse();
        assertThat(route.matches("/cookie-monster")).isTrue();
        assertThat(route.matches("/1234")).isTrue();
        
        Map<String, String> params = route.getPathParametersEncoded("/cookie-monster");
        assertThat(params.containsKey("name")).isTrue();
        assertThat(params.get("name")).isEqualTo("cookie-monster");
    }
}
