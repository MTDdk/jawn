package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.AssertionsHelper;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;

class ValueParserTest {

    @Test
    void value_int() {
        Object result = ValueParser.to(Value.of("73"), int.class);
        assertTrue(result instanceof Integer);
        assertEquals(73, result);
        
        result = ValueParser.to(Value.of("730"), Integer.class);
        assertTrue(result instanceof Integer);
        assertEquals(730, result);
    }

    @Test
    void value_long() {
        Object result = ValueParser.to(Value.of("100100100100"), long.class);
        assertTrue(result instanceof Long);
        assertEquals(100_100_100_100l, result);
        
        result = ValueParser.to(Value.of("730"), Long.class);
        assertTrue(result instanceof Long);
        assertEquals(730l, result);
    }
    
    @Test
    void value_double() {
        Object result = ValueParser.to(Value.of("123.123"), double.class);
        assertTrue(result instanceof Double);
        assertEquals(123.123, result);
        
        result = ValueParser.to(Value.of("321.321"), Double.class);
        assertTrue(result instanceof Double);
        assertEquals(321.321, result);
    }
    
    @Test
    void value_boolean() {
        Object result = ValueParser.to(Value.of("true"), boolean.class);
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
        
        result = ValueParser.to(Value.of("f"), Boolean.class);
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }
    
    @Test
    void value_fail() {
        assertThrowsExactly(Up.ParseError.class, () -> ValueParser.to(Value.empty(), ValueParserTest.class));
    }
    
    @Test
    void toCollection_boolean() {
        Collection<Boolean> collection = ValueParser.toCollection(Value.of(Value.of("true"), Value.of("true"), Value.of("false")), boolean.class, new ArrayList<Boolean>());
        assertEquals(3, collection.size());
        AssertionsHelper.ass(collection, true, true, false);
    }
    
    @Test
    void toCollection_missingValues() {
        Collection<String> collection = ValueParser.toCollection(Value.of(Value.empty(), Value.empty(), Value.of("true")), String.class, new ArrayList<String>());
        assertEquals(1, collection.size());
        AssertionsHelper.ass(collection, "true");
    }

}
