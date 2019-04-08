package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;

public class HttpHandlerImplTest {

    @Test
    public void beforeSimpleResultOverride() {
        Context context = mock(Context.class);
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler(() -> Results.noContent())
            .before((c,ch) -> Results.ok())
            .build();
        
        
        // execute
        Result result = HttpHandlerImpl._handle(context, handler, r -> {});//handler.handle(context);
        assertThat(result.status().isPresent()).isTrue();
        result.status().ifPresentOrElse(status -> assertThat(status.value()).isEqualTo(200), Assert::fail);
    }
    
    @Test(expected = Up.BadResult.class)
    public void throw_when_afterReturnsNull() {
        Route handler = new Route.Builder(HttpMethod.GET)
            .path("/")
            .handler(() -> Results.ok())
            .after((c,r) -> (Result) null)
            .build();
        
        Context context = mock(Context.class);
        HttpHandlerImpl._handle(context, handler, r -> {}); //throws
        
        Assert.fail();
    }
    
    @Test(expected = Up.BadResult.class)
    public void throw_when_handlerReturnsNull() {
        Route handler = new Route.Builder(HttpMethod.GET)
            .path("/")
            .handler(c -> (Result) null)
            .build();
        
        Context context = mock(Context.class);
        HttpHandlerImpl._handle(context, handler, r -> {}); //throws
        
        Assert.fail();
    }
    
    @Test
    public void afterSimpleResultOverride() {
        Context context = mock(Context.class);
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler(() -> Results.noContent())
            .after((c,r) -> Results.ok())
            .build();
        
        // execute
        Result result = HttpHandlerImpl._handle(context, handler, r -> {});//handler.handle(context);
        
        // assert
        assertThat(result.status().isPresent()).isTrue();
        result.status().ifPresentOrElse(status -> assertThat(status.value()).isEqualTo(200), Assert::fail);
    }

    @Test
    public void after_should_alwayBeExecuted() {
        boolean[] executed = new boolean[3];
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler(() -> { executed[0]=true; return Results.noContent(); })
            .before((c,ch) -> Results.ok())
            .before((c,ch) -> { executed[2]=true; return Results.notFound(); })
            .after((c,r) -> {executed[1] = true; return r; })
            .build();
        
        
        Context context = mock(Context.class);
        Result result = HttpHandlerImpl._handle(context, handler, r -> {});//handler.handle(context);
        
        assertThat(executed[0]).isFalse();
        assertThat(executed[1]).isTrue();
        assertThat(executed[2]).isFalse();
        assertThat(result.status().get()).isEqualTo(Status.OK);
    }

}
