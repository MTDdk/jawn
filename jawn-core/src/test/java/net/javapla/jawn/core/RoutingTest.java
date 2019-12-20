package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.NoSuchFileException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.truth.Correspondence;
import com.google.inject.Injector;

public class RoutingTest {

    private static Injector injector;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DeploymentInfo di = mock(DeploymentInfo.class);
        //when(di.getRealPath(eq(""))).thenReturn("");
        when(di.listResources(eq(""))).thenThrow(NoSuchFileException.class);
        
        injector = mock(Injector.class);
        when(injector.getInstance(DeploymentInfo.class)).thenReturn(di);
    }


    @Test
    public void pathPrefix() {
        Jawn j = new Jawn();
        j.path("/prefix", () -> {
            j.get("/first", Results.ok());
            j.post("/second", Results.ok());
            j.put("/third", Results.ok());
            j.delete("/fourth", Results.noContent());
        });
        
        List<Route> routes = j.buildRoutes(injector);
        
        assertThat(routes).hasSize(4);
        assertThat(routes).comparingElementsUsing(routePathComparer).containsExactly("/prefix/first","/prefix/second","/prefix/third","/prefix/fourth");
    }
    
    @Test
    public void mix() {
        Jawn j = new Jawn();
        j.get("/nothing", Results.ok());
        j.path("/prefix", () -> {
            j.post("/tail", Results.ok());
            
            j.path("/secondlevel", () -> {
                j.put("/tail", Results.ok());
            });
        });
        
        List<Route> routes = j.buildRoutes(injector);
        
        assertThat(routes).hasSize(3);
        assertThat(routes).comparingElementsUsing(routePathComparer).containsExactly("/prefix/tail", "/nothing", "/prefix/secondlevel/tail");
    }

    
    Correspondence<Route, String> routePathComparer = new Correspondence<Route, String>() {
        @Override
        public boolean compare(Route actual, String expected) {
            return actual.path().equals(expected);
        }

        @Override
        public String toString() {
            return null;
        }
    };
}
