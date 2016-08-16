package net.javapla.jawn.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResponseTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void parallisedExecution() {
        int executionTime = 500;
        Response resp1 = ResponseBuilder.ok().renderable(() -> {
            System.out.println("start 1");
            Thread.sleep(executionTime);
            return "done 1";
        });
        
        Response resp2 = ResponseBuilder.ok().renderable(() -> {
            System.out.println("start 2");
            Thread.sleep(executionTime);
            return "done 2";
        });
        
        Response resp3 = ResponseBuilder.ok().renderable(() -> {
            System.out.println("start 3");
            Thread.sleep(executionTime);
            return "done 3";
        });
        
        long time = System.currentTimeMillis();
        System.out.println(resp1.renderable());
        System.out.println(resp2.renderable());
        System.out.println(resp3.renderable());
        time = System.currentTimeMillis() - time;
        System.out.println(time);
        assertTrue(time < executionTime*3);
    }

}
