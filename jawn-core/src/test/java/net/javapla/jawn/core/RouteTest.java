package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class RouteTest {
    
    @Test
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
    }

    @Test
    public void throw_when_afterReturnsNull() {
        Route handler = new Route.Builder(HttpMethod.GET)
            .path("/")
            .handler((c) -> Results.ok())
            .after((c,r) -> (Result) null)
            .build();
        
        Context context = mock(Context.class);
        
        assertThrows(Up.BadResult.class, () -> handler.handle(context));
    }
    
    @Test
    public void throw_when_handlerReturnsNull() {
        Route handler = new Route.Builder(HttpMethod.GET)
            .path("/")
            .handler(c -> (Result) null)
            .build();
        
        Context context = mock(Context.class);
        
        assertThrows(Up.BadResult.class, () -> handler.handle(context));
    }

}
