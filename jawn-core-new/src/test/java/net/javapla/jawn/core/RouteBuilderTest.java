package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import net.javapla.jawn.core.Route.Chain;

public class RouteBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void emptyPath() {
        new Route.Builder(HttpMethod.GET).path("");
    }
    
    @Test(expected = NullPointerException.class)
    public void nullPath() {
        String s = null;
        new Route.Builder(HttpMethod.GET).path(s);
    }

    @Test(expected = NullPointerException.class)
    public void nullPathBuild() {
        new Route.Builder(HttpMethod.GET).build(); 
    }
    
    @Test
    public void filterGivesBeforeAndAfter() {
        Route route = new Route
            .Builder(HttpMethod.POST)
            .path("/")
            .filter(new Route.Filter() {
                @Override
                public Result before(Context context, Chain chain) {
                    return chain.next();
                }
                
                @Override
                public Result after(Context context, Result result) {
                    return null;
                }
            }).build();
        
        assertThat(route.before()).isNotNull();
        assertThat(route.after()).isNotNull();
    }
    
}
