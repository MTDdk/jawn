package net.javapla.jawn.core;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Try {
    
    static <R> Optional<R> with(ThrowingSupplier<R> s) {
        try {
            return Optional.of(s.get());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }
    
    static void perform() {
        
    }
    
    
    interface ThrowingRunnable extends Runnable {
        void tryRun() throws Throwable;
        
        @Override
        default void run() {
            try {
                tryRun();
            } catch (Throwable e) {
                
            }
        }
    }
    interface ThrowingSupplier<R> extends Supplier<R> {
        R tryGet() throws Throwable;
        
        @Override
        default R get() {
            try {
                return tryGet();
            } catch (Throwable e) {
                return null;
            }
        }
    }
    interface ThrowingFunction<T, RESULT> {
        RESULT apply(T t) throws Throwable;
    }
    
    interface Exceptional<RESULT> {
        RESULT exception(Consumer<? super Throwable> c);
    }
    
    interface Work extends Exceptional<Work> {
        Work perform(ThrowingRunnable r);
    }
}
