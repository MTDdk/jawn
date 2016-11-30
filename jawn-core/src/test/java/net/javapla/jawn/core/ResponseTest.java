package net.javapla.jawn.core;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class ResponseTest {

    @Test
    public void parallisedExecution() {
        int executionTime = 400;
        int executionOverhead = 70; //approximation
        
        List<Response> responses = IntStream.range(1, 4).boxed().map(i -> 
            ResponseBuilder.ok().renderable(() -> {
                System.out.println("start " + i);
                Thread.sleep(executionTime);
                return "done " + i;
            })
        ).collect(Collectors.toList());
        
        long time = System.currentTimeMillis();
        responses.forEach(resp ->  System.out.println(resp.renderable()));
        time = System.currentTimeMillis() - time;
        System.out.println(time + " compared to " + (executionTime + executionOverhead));
        assertTrue(time < executionTime + executionOverhead);
    }
    
}
