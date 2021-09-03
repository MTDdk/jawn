package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;

public class RouteBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void emptyPath() {
        new Route.BuilderImpl(HttpMethod.GET, "", Route.NOT_FOUND);
    }
    
    @Test(expected = NullPointerException.class)
    public void nullPath() {
        String s = null;
        new Route.BuilderImpl(HttpMethod.GET, s, Route.NOT_FOUND);
    }

    /*@Test(expected = NullPointerException.class)
    public void nullPathBuild() {
        new Route.Builder(HttpMethod.GET).build(); 
    }*/
    
    /*@Test
    public void filterGivesBeforeAndAfter() {
        Route route = new Route
            .Builder(HttpMethod.POST)
            .path("/")
            .filter(new Route.Filter() {
                @Override
                public Result before(Context context, Route.Chain chain) {
                    return chain.next(context);
                }
                
                @Override
                public Result after(Context context, Result result) {
                    return null;
                }
            }).build();
        
        assertThat(route.before()).isNotNull();
        assertThat(route.after()).isNotNull();
    }*/
    
    @Test
    public void emptyBefores() {
        assertThat(new Route.BuilderImpl(HttpMethod.GET, "/", Route.NOT_FOUND).build(mock(RendererEngineOrchestrator.class)).before()).isNull();
    }
    
    @Test
    public void emptyAfters() {
        assertThat(new Route.BuilderImpl(HttpMethod.GET, "/", Route.NOT_FOUND).build(mock(RendererEngineOrchestrator.class)).after()).isNull();
    }
}
