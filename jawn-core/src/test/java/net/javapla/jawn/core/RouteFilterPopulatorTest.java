package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.NoSuchFileException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Key;

public class RouteFilterPopulatorTest {
    
    private static Injector injector;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void beforeClass() throws Exception {
        DeploymentInfo di = mock(DeploymentInfo.class);
        when(di.listResources(eq(""))).thenThrow(NoSuchFileException.class);
        
        injector = mock(Injector.class);
        when(injector.getInstance(DeploymentInfo.class)).thenReturn(di);
        
        /*when(injector.getInstance(any(Class.class))).then(c -> {
            Class<?> argument = c.getArgument(0);
            
            if (argument.isAssignableFrom(DeploymentInfo.class)) return di;
            
            // in case of F,B,A
            return argument.getDeclaredConstructor().newInstance();
        });*/
        when(injector.getInstance(any(Key.class))).then(k -> {
            Key<?> argument = k.getArgument(0);
            
            // in case of F,B,A
            return argument.getTypeLiteral().getRawType().getDeclaredConstructor().newInstance();
        });
    }

    @Test
    public void orderingOfFilters() {
        // filters should be executed in correct order
        // filter1.before -> filter2.before -> filter3.before -> routeSpecificBefore
        // -> execute handler
        // -> routeSpecificAfter -> filter3.after -> filter2.after -> filter1.after
        
        Jawn j = new Jawn();
        Result result = Results.noContent();
        long[] executionOrder = new long[8];
        
        j.get("/", result)
            .before(() -> {executionOrder[3] = System.nanoTime();})
            .after(() -> {executionOrder[4] = System.nanoTime();})
            ;
        
        j.filter(new Route.Filter() { //filter1
            @Override
            public Result before(Context context, Route.Chain chain) {
                executionOrder[0] = System.nanoTime();
                return chain.next(context);
            }
            @Override
            public Result after(Context context, Result result) {
                executionOrder[7] = System.nanoTime();
                return result;
            }
        });
        j.filter(new Route.Filter() { //filter2
            @Override
            public Result before(Context context, Route.Chain chain) {
                executionOrder[1] = System.nanoTime();
                return chain.next(context);
            }
            @Override
            public Result after(Context context, Result result) {
                executionOrder[6] = System.nanoTime();
                return result;
            }
        });
        j.filter(new Route.Filter() { //filter3
            @Override
            public Result before(Context context, Route.Chain chain) {
                executionOrder[2] = System.nanoTime();
                return chain.next(context);
            }
            @Override
            public Result after(Context context, Result result) {
                executionOrder[5] = System.nanoTime();
                return result;
            }
        });
        
        // execute
        Context context = mock(Context.class);
        j.buildRoutes(injector).forEach(r -> {
            r.handle(context);
        });
        
        // by asking if the executionOrder is ordered,
        // then the before/after-relation will be established according to
        // how the ordering of the filters *should* be 
        assertThat(executionOrder).asList().isInOrder();
    }

    
    @Test
    public void uninstantiatedFilters() {
        Jawn j = new Jawn();
        j.filter(F.class);
        j.filter(B.class);
        j.filter(A.class);
        
        j.get("/", () -> Results.ok());
        
        List<Route> routes = j.buildRoutes(injector);
        assertThat(routes).hasSize(1);
        
        Route route = routes.get(0);
        assertThat(route.before()).isNotNull();
        assertThat(route.before()).hasLength(2);
        assertThat(route.after()).hasLength(2);
    }
    
    static class F implements Route.Filter {
        @Override
        public Result before(Context context, Route.Chain chain) {
            return null;
        }

        @Override
        public Result after(Context context, Result result) {
            return null;
        }
    }
    
    static class B implements Route.Before {
        @Override
        public Result before(Context context, Route.Chain chain) {
            return null;
        }
    }
    
    static class A implements Route.After {
        @Override
        public Result after(Context context, Result result) {
            return null;
        }
    }
}
