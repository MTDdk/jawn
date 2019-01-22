package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import net.javapla.jawn.core.Route.Chain;

public class RouteFilterPopulatorTest {

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
            public Result before(Context context, Chain chain) {
                executionOrder[0] = System.nanoTime();
                return chain.next();
            }
            @Override
            public Result after(Context context, Result result) {
                executionOrder[7] = System.nanoTime();
                return result;
            }
        });
        j.filter(new Route.Filter() { //filter2
            @Override
            public Result before(Context context, Chain chain) {
                executionOrder[1] = System.nanoTime();
                return chain.next();
            }
            @Override
            public Result after(Context context, Result result) {
                executionOrder[6] = System.nanoTime();
                return result;
            }
        });
        j.filter(new Route.Filter() { //filter3
            @Override
            public Result before(Context context, Chain chain) {
                executionOrder[2] = System.nanoTime();
                return chain.next();
            }
            @Override
            public Result after(Context context, Result result) {
                executionOrder[5] = System.nanoTime();
                return result;
            }
        });
        
        // execute
        Context context = mock(Context.class);
        j.buildRoutes().forEach(r -> {
            r.handle(context);
        });
        
        // by asking if the executionOrder is ordered,
        // then the before/after-relation will be established according to
        // how the ordering of the filters *should* be 
        assertThat(executionOrder).asList().isOrdered();
    }

}
