package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class ValueTest {

    @Test
    public void asString() {
        Value value = Value.of("4000");
        
        assertThat(value.value()).isEqualTo("4000");
        
        String result = value.value();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo("4000");
    }
    
    @Test
    public void asOptional() {
        Value value = Value.of("4000");
        Optional<String> optional = value.toOptional();
        
        assertThat(optional).isInstanceOf(Optional.class);
        assertThat(optional.get()).isEqualTo("4000");
    }
    
    @Test
    public void asTypedOptional() {
        Value value = Value.of("4000");
        Optional<Long> optional = value.toOptional(long.class);
        
        assertThat(optional).isInstanceOf(Optional.class);
        assertThat(optional.get()).isEqualTo(4000);
    }
    
    @Test
    public void emptyOptional() {
        Value value = Value.of((String)null);
        assertThat(value.toOptional().isPresent()).isFalse();
        
        value = Value.of("");
        assertThat(value.toOptional().isPresent()).isFalse();
    }

    @Test
    public void asDouble() {
        Value value = Value.of("4000.3");
        assertThat(value.doubleValue()).isEqualTo(4000.3);
    }
    
    @Test
    public void asLong() {
        Value value = Value.of("4000000004");
        assertThat(value.longValue()).isEqualTo(4_000_000_004l);
    }
    
    @Test
    public void withFallback() {
        Value value = Value.of("4000.3");
        assertThat(value.intValue(333)).isEqualTo(333);
    }
    
    @Test(expected = Up.ParsableError.class)
    public void unparsable() {
        Value value = Value.of("false");
        value.intValue();
    }
    
    @Ignore(value = "It is no longer illegal to use primitive types")
    @Test(expected = IllegalArgumentException.class)
    public void primitiveNotAllowedAsGeneric() {
        Value value = Value.of("4000");
        value.toOptional(int.class);
    }
    
    @Test
    public void asStringList() {
        List<String> list = Value.of("aa", "bb").toList();//new ValueImpl(engine, new ListParsable("aa", "bb")).toList();
        assertThat(list).containsExactly("aa", "bb");
    }
    
    @Test
    public void asStringSet() {
        Set<String> set = Value.of("aa", "bb", "bb", "cc").toSet();
        assertThat(set).containsExactly("aa", "bb", "cc");
    }
    
    @Test
    public void emptyList() {
        List<String> list = Value.of(((String)null)).toList();
        assertThat(list).isEmpty();
    }
    
    @Test
    public void mapString() {
        String otherValue = "true";
        boolean result = Value.of("true").map(otherValue::equals).orElse(false);
        assertThat(result).isTrue();
        
        result = Value.of("false").map(otherValue::equals).orElse(true);
        assertThat(result).isFalse();
    }
    
    @Test
    public void convertAndMap() {
        long lastModified = 1080;
        boolean result = Value.of("1080").map(long.class, modifiedSince -> lastModified <= modifiedSince).orElse(false);
        assertThat(result).isTrue();
        
        result = Value.of("640").map(long.class, modifiedSince -> lastModified <= modifiedSince).orElse(false);
        assertThat(result).isFalse();
    }
    
    @Test
    public void asEnum() {
        Value value = Value.of("A");
        assertThat(value.toEnum(LETTER.class)).isEqualTo(LETTER.A);
    }
    
    @Test
    public void asEnumList() {
        Value value = Value.of("A", "B");
        List<LETTER> list = value.toList(LETTER.class);
        
        //List<LETTER> list = ValueImpl.of(Parsable.of("A", "B")).toList(LETTER.class);
        assertThat(list).containsExactly(LETTER.A, LETTER.B);
    }
    
    enum LETTER { A,B }
}
