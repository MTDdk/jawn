package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;

public class ValueParserTest {

    @Test
    public void value_int() {
        Object result = ValueParser.to(Value.of("73"), int.class);
        assertThat(result).isInstanceOf(Integer.class);
        assertThat(result).isEqualTo(73);
        
        result = ValueParser.to(Value.of("730"), Integer.class);
        assertThat(result).isInstanceOf(Integer.class);
        assertThat(result).isEqualTo(730);
    }

    @Test
    public void value_long() {
        Object result = ValueParser.to(Value.of("100100100100"), long.class);
        assertThat(result).isInstanceOf(Long.class);
        assertThat(result).isEqualTo(100_100_100_100l);
        
        result = ValueParser.to(Value.of("730"), Long.class);
        assertThat(result).isInstanceOf(Long.class);
        assertThat(result).isEqualTo(730);
    }
    
    @Test
    public void value_double() {
        Object result = ValueParser.to(Value.of("123.123"), double.class);
        assertThat(result).isInstanceOf(Double.class);
        assertThat(result).isEqualTo(123.123);
        
        result = ValueParser.to(Value.of("321.321"), Double.class);
        assertThat(result).isInstanceOf(Double.class);
        assertThat(result).isEqualTo(321.321);
    }
    
    @Test
    public void value_boolean() {
        Object result = ValueParser.to(Value.of("true"), boolean.class);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(true);
        
        result = ValueParser.to(Value.of("f"), Boolean.class);
        assertThat(result).isInstanceOf(Boolean.class);
        assertThat(result).isEqualTo(false);
    }
    
    @Test
    public void value_fail() {
        assertThrows(Up.ParsableError.class, () -> ValueParser.to(Value.empty(), ValueParserTest.class));
    }
    
    @Test
    public void toCollection_boolean() {
        Collection<Boolean> collection = ValueParser.toCollection(Value.of(Value.of("true"), Value.of("true"), Value.of("false")), boolean.class, new ArrayList<Boolean>());
        assertThat(collection).hasSize(3);
        assertThat(collection).containsExactly(true, true, false);
    }
    
    @Test
    public void toCollection_missingValues() {
        Collection<String> collection = ValueParser.toCollection(Value.of(Value.empty(), Value.empty(), Value.of("true")), String.class, new ArrayList<String>());
        assertThat(collection).hasSize(1);
        assertThat(collection).containsExactly("true");
    }
}
