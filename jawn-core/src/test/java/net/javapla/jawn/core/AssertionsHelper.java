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
    
    public static <T> void ass(T[] act, @SuppressWarnings("unchecked") T ... exp) {
        assertEquals(exp.length, act.length);
        
        int i = 0;
        for (T a : act) {
            assertEquals(exp[i++], a);
        }
    }
    
    public static void ass(CharSequence exp, char[] act) {
        assertEquals(exp.length(), act.length);
        
        for (int i = 0; i < act.length; i++) {
            assertEquals(exp.charAt(i), act[i]);
        }
    }
}
