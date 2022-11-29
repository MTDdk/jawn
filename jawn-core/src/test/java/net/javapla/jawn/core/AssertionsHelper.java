package net.javapla.jawn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

public class AssertionsHelper {

    public static <T> void ass(T[] exp, Collection<T> act) {
        assertEquals(exp.length, act.size());
        
        int i = 0;
        for (T a : act) {
            assertEquals(exp[i++], a);
        }
    }
    
    public static <T> void ass(Collection<T> act, @SuppressWarnings("unchecked") T ... exp) {
        assertEquals(exp.length, act.size());
        
        int i = 0;
        for (T a : act) {
            assertEquals(exp[i++], a);
        }
    }
}
