package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Route.Chain;
import net.javapla.jawn.core.Route.RouteHandler;

public class RouteBuilderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

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
        RouteHandler route = new Route
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
