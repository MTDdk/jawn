package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

public class RouteFilterExecutionTest {

    @Test
    public void emptyBefores() {
        assertThat(new Route.Builder(HttpMethod.GET).path("/").build().before()).isNull();
    }
    
    @Test
    public void emptyAfters() {
        assertThat(new Route.Builder(HttpMethod.GET).path("/").build().after()).isNull();
    }
    
    @Test
    public void beforeSimpleResultOverride() {
        Context context = mock(Context.class);
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler(() -> Results.noContent())
            .before(Results.ok())
            .build();
        
        // execute
        Result result = handler.handle(context);
        assertThat(result.status().isPresent()).isTrue();
        result.status().ifPresentOrElse(status -> assertThat(status.value()).isEqualTo(200), Assert::fail);
    }
    
    @Test
    public void afterSimpleResultOverride() {
        Context context = mock(Context.class);
        Route handler = new Route.Builder(HttpMethod.GET).
            path("/")
            .handler(() -> Results.noContent())
            .after(Results.ok())
            .build();
        
        // execute
        Result result = handler.handle(context);
        
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
            .before(Results.ok())
            .before(() -> { executed[2]=true; return Results.notFound(); })
            .after(() -> executed[1] = true)
            .build();
        
        
        Context context = mock(Context.class);
        Result result = handler.handle(context);
        
        assertThat(executed[0]).isFalse();
        assertThat(executed[1]).isTrue();
        assertThat(executed[2]).isFalse();
        assertThat(result.status).isEqualTo(Status.OK);
    }
    
    @Test(expected = Up.BadResult.class)
    public void throw_when_afterReturnsNull() {
        Route handler = new Route.Builder(HttpMethod.GET)
            .path("/")
            .handler(() -> Results.ok())
            .after((Result) null)
            .build();
        
        Context context = mock(Context.class);
        handler.handle(context); //throws
        
        Assert.fail();
    }
    
    @Test(expected = Up.BadResult.class)
    public void throw_when_handlerReturnsNull() {
        Route handler = new Route.Builder(HttpMethod.GET)
            .path("/")
            .handler(c -> (Result) null)
            .build();
        
        Context context = mock(Context.class);
        handler.handle(context); //throws
        
        Assert.fail();
    }
}
