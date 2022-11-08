package net.javapla.jawn.core.internal.reflection;

import java.io.Serializable;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

import net.javapla.jawn.core.Up;


public final class Reflection {

    @SuppressWarnings("unchecked")
    public static final <T> Class<T> callingClass(Class<T> assignableFrom) {

        /* 
         * https://stackoverflow.com/a/34948763
         *  - index 0 = Thread
         *  - index 1 = this
         *  - index 2 = direct caller, can be self.
         *  - index 3 ... n = classes and methods that called each other to get to the index 2 and below.
         */

        StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
        return (Class<T>) walker.walk( frame -> {
            return frame
                .map(StackFrame::getDeclaringClass)
                .skip(2)
                .filter(clz -> assignableFrom.isAssignableFrom(((Class<?>)clz.getGenericSuperclass())))
                .findFirst();
        }).get();

        /*StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stack.length; i++) {

            //Class<?> compiledClass = getCompiledClass(stack[i].getClassName(), false);
            try {
                Class<?> compiledClass = Reflection.class.getClassLoader().loadClass(stack[i].getClassName());
                if (assignableFrom.isAssignableFrom(compiledClass)) {
                    return (Class<T>) compiledClass;
                }
            } catch (ClassNotFoundException e) {
                throw Up.IO(e);
            }
        }

        return null;*/
    }


    public static Method getLambdaMethod(ClassLoader loader, Object function) throws Exception {
        SerializedLambda lambda = getSerializedLambda(function);
        if (lambda != null) {
            String implClassName = lambda.getImplClass().replace('/', '.');
            Class<?> implClass = loader.loadClass(implClassName);//Class.forName(implClassName);

            String lambdaName = lambda.getImplMethodName();

            for (Method m : implClass.getDeclaredMethods()) {
                if (m.getName().equals(lambdaName)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Looks for lambdas implementing {@link Serializable}
     * @param function
     * @return
     * @throws NoSuchMethodException
     */
    private static SerializedLambda getSerializedLambda(Object function) throws NoSuchMethodException {
        for (Class<?> clazz = function.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method replaceMethod = clazz.getDeclaredMethod("writeReplace");
                replaceMethod.setAccessible(true);
                Object serializedForm = replaceMethod.invoke(function);

                if (serializedForm instanceof SerializedLambda) {
                    return (SerializedLambda) serializedForm;
                }
            } catch (NoSuchMethodException e) {
                // fall through the loop and try the next class
            } catch (Exception t) {
                throw Up.ParseError("", t);
            }
        }

        return null;
    }
    
}

