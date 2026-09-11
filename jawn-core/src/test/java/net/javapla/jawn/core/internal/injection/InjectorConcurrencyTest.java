package net.javapla.jawn.core.internal.injection;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.annotation.Singleton;

class InjectorConcurrencyTest {

    @Test
    void concurrent() throws InterruptedException, ExecutionException {
        Injector injector = new Injector();
        
        TestClass ts1,ts2,ts3;
        
        ForkJoinTask<TestClass> task1 = ForkJoinPool.commonPool().submit(() -> injector.require(TestClass.class));
        ForkJoinTask<TestClass> task2 = ForkJoinPool.commonPool().submit(() -> injector.require(TestClass.class));
        ForkJoinTask<TestClass> task3 = ForkJoinPool.commonPool().submit(() -> injector.require(TestClass.class));
        
        ts1 = task1.get();
        ts2 = task2.get();
        ts3 = task3.get();
        
        assertEquals(ts1, ts2);
        assertEquals(ts1, ts3);
    }

    @Singleton
    static class TestClass {
        public TestClass() {}
    }
}
