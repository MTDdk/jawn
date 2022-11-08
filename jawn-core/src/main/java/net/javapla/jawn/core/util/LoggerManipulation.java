package net.javapla.jawn.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for manipulate Logging Frameworks
 * 
 * <p>
 * The core module only depends on Slf4J, which is just an abstraction,
 * on which other logging frameworks rely, but does not itself hold any
 * implementation or logic for setting configurations at runtime.
 * 
 * <p>
 * This class does some trickery to try to manipulate Loggers at runtime
 * without knowing the implementing framework, as some 3rd party libraries
 * are known to be a bit excessive in their logging, and instead of
 * all projects using Jawn having to explicitly disable logging for a 
 * bunch of unknown transitive libraries, this class tries to
 * silence loggers without knowing what logging framework implementing Slf4J
 * is actually being used.
 * 
 * <p>
 * How to use:
 * 
 * <p>
 * <pre>
    static {
        LoggerManipulation.quiet("org.jboss.threads");
        LoggerManipulation.quiet("org.xnio.nio");
        LoggerManipulation.quiet("io.undertow");
    }
 * </pre>
 *
 */
public abstract class LoggerManipulation {

    public static final void quiet(String loggerName) {
        Logger logger = LoggerFactory.getLogger(loggerName);

        // logger.setAdditive(false);
        setAdditive(logger);
        
        
        // logger.setLevel(Level.WARN);
        setLogLevel(logger, "WARN");
    }
    
    public static void setAdditive(Logger logger) {
        try {
            logger
                .getClass()
                .getDeclaredMethod("setAdditive", boolean.class) // throws if the method does not exist
                .invoke(logger, false);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            // The method seems to not exist
        }
    }
    
    public static void setLogLevel(Logger logger, String loglevel) {
        try {
            // Look up the field.
            // We can use the type of the field to look up the corresponding setter method
            Field level = logger.getClass().getDeclaredField("level"); // throws if this does not exist
            
            // Hopefully there is a setter method
            Method method = logger.getClass().getDeclaredMethod("setLevel", level.getType());
            
            // Look up the values of the field type.
            // Should work for both enum and classes and is thus agnostic 
            // to what type the logging framework is having its levels defined
            for (var f : level.getType().getFields()) {
                if (f.getName().equals(loglevel)) {
                    method.invoke(logger, f.get(level));
                }
            }
            
        } catch (NoSuchFieldException e) {
            
        } catch (NoSuchMethodException e) {
            
        } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
            
        }
    }
}
