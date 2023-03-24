package net.javapla.jawn.core.internal.reflection;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.TypeLiteral;

class ReturnTypeTest {
    
    private RouteClassAnalyser analyser = new RouteClassAnalyser(new ClassSource(getClass().getClassLoader()), false);

    @Test
    void returnTypes() { // literals
        
        // arrays
        assertType(boolean[].class, ctx -> new boolean[] {true, false, true, false});
        assertType(int[].class, ctx -> new int[] {0,1,-1});
        assertType(double[].class, ctx -> new double[] {0,1,-1, 3.1415926});
        
        assertType(String.class, ctx -> "some string");
        assertType(Integer.class, ctx -> 16);
        
        // return type of a method call
        assertType(PrimitiveHandler.class, ctx -> {
            TestInstance instance = new TestInstance();
            return instance.newInstance(0, "v");
        });
        assertType(Integer.class, ctx -> TestInstance.testing());
        assertType(String.class, ctx -> {
            Object o = new Object();
            return o.toString();
        });
        
        
        // TODO perhaps this notion can in some way get implemented into the TypeParser
        // The primitive is only visible because the ASM eventually finds the "int primitive(Context ctx);",
        // but in any other case, the int (and other primitives) will be autoboxed because of the return type of
        // Object in the fHandler "Object handle(Context ctx) throws Exception"
        assertPrimitive(int.class, ctx -> 16); 
    }
    
    @Test
    void lambdaInvocation() {
        assertType(String.class, Context::toString);
    }
    
    @Test
    void callable() {
        assertType(TypeLiteral.getParameterized(Callable.class, Short.class), ctx -> {
            Callable<Short> callable = () -> Short.valueOf((short) 3);
            return callable;
        });

        assertType(TypeLiteral.getParameterized(Callable.class, Character.class), ctx -> {
            Callable<Character> callable = () -> 'k';
            return callable;
        });

        assertType(TypeLiteral.getParameterized(Callable.class, Object.class), ctx -> (Callable<Object>) () -> new TestInstance());
    }

    private void assertType(Type expected, Route.Handler handler) {
        assertEquals(expected.getTypeName(), analyser.returnType(handler).getTypeName());
    }
    
    private void assertPrimitive(Type expected, PrimitiveHandler handler) {
        assertEquals(expected.getTypeName(), analyser.returnType(handler).getTypeName());
    }
    
    private void assertType(TypeLiteral<?> expected, Route.Handler handler) {
        assertType(expected.type, handler);
    }
    
    
    interface PrimitiveHandler extends Route.Handler {
        int primitive(Context ctx);
        
        @Override
        default Object handle(Context ctx) throws Exception {
            return primitive(ctx);
        }
    }
    
    static class TestInstance {
        public PrimitiveHandler newInstance(int x, String v) {
            return c -> 73;
        }
        
        public static int testing() {
            String s = "nothing";
            return s.length();
        }
    }
}
