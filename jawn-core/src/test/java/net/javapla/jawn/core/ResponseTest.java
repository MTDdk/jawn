package net.javapla.jawn.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;


public class ResponseTest {
    
    @Test
    public void renderable() {
        Object test = new Object();
        Result response = ResultBuilder.ok().renderable(test);
        assertEquals(test, response.renderable());
    }
    
    @Test
    public void contentType() {
        String contentType = "text/something-proprietary";
        Result response = ResultBuilder.ok().contentType(contentType);
        assertEquals(contentType, response.contentType());
    }

    @Test
    @Ignore
    public void parallisedExecution() {
        int executionTime = 400;
        int executionOverhead = 80; //approximation
        
        List<Result> responses = IntStream.range(1, 8).boxed().map(i -> 
            ResultBuilder.ok().renderable(() -> {
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
    
    @Test
    public void futureExecutionWithError() {
        Result response = ResultBuilder.ok().renderable(() -> {System.out.println("throwing exception"); throw new Exception();});
        Object renderable = response.renderable();
        assertTrue(renderable instanceof Future);
    }
    
}
