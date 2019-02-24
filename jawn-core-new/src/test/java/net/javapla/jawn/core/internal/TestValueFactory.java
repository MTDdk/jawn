package net.javapla.jawn.core.internal;

import java.nio.charset.StandardCharsets;

import com.google.inject.Guice;

import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.parsers.Parsable;
import net.javapla.jawn.core.parsers.ParserEngineManager;

/**
 * Purely for testing purposes
 */
public class TestValueFactory {

    public static Value of(String param) {
        return ValueImpl.of(/*Guice.createInjector().getInstance(ParserEngineManager.class), */Parsable.of(param));
    }
    
    public static Value of(String ... param) {
        return ValueImpl.of(/*Guice.createInjector().getInstance(ParserEngineManager.class), */Parsable.of(param));
    }
    
    public static Value of(byte[] bytes) {
        return ComplexValueImpl.of(Guice.createInjector().getInstance(ParserEngineManager.class), Parsable.of(bytes, StandardCharsets.UTF_8));
    }
}
