package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
        
        assertThat(Value.of("test").value(s -> s + "ing", "fallback")).isEqualTo("testing");
    }
    
    @Test
    public void asOptional() {
        Value value = Value.of("4000");
        Optional<String> optional = value.asOptional();
        
        assertThat(optional).isInstanceOf(Optional.class);
        assertThat(optional.get()).isEqualTo("4000");
    }
    
    @Test
    public void asTypedOptional() {
        Value value = Value.of("4000");
        Optional<Long> optional = value.asOptional(long.class);
        
        assertThat(optional).isInstanceOf(Optional.class);
        assertThat(optional.get()).isEqualTo(4000);
    }
    
    @Test
    public void emptyOptional() {
        Value value = Value.of((String)null);
        assertThat(value.asOptional().isPresent()).isFalse();
        
        value = Value.of("");
        assertThat(value.asOptional().isPresent()).isFalse();
    }
    
    @Test
    public void asInt() {
        Value value = Value.of("400");
        assertThat(value.asInt()).isEqualTo(400);
        assertThat(value.asInt(333)).isEqualTo(400);
        assertThat(value.asInt(i -> i/4, 333)).isEqualTo(100);
    }

    @Test
    public void asDouble() {
        Value value = Value.of("4000.3");
        assertThat(value.asDouble()).isEqualTo(4000.3);
        assertThat(value.asDouble(333)).isEqualTo(4000.3);
        assertThat(value.asDouble(i -> i-1000, 333)).isEqualTo(3000.3);
    }
    
    @Test
    public void asLong() {
        Value value = Value.of("4000000004");
        assertThat(value.asLong()).isEqualTo(4_000_000_004l);
        assertThat(value.asLong(333)).isEqualTo(4_000_000_004l);
        assertThat(value.asLong(i -> i/4,333)).isEqualTo(1_000_000_001l);
    }
    
    @Test
    public void dateAsEpoch() {
        assertThat(Value.of("Fri, 22 Nov 2019 13:42:41 GMT").asLong()).isEqualTo(1574430161000l);
        
        assertThrows(Up.ParsableError.class, () -> Value.of("Thu, 22 Nov 2019 13:42:41 GMT").asLong());// <-- was a Friday, not Thursday
        assertThrows(Up.ParsableError.class, () -> Value.of("22 Nov 2019 13:42:41 GMT").asLong()); // <-- only RFC-1123; https://tools.ietf.org/html/rfc1123
    }
    
    @Test
    public void withFallback() {
        Value value = Value.of("4000.3");
        assertThat(value.asInt(333)).isEqualTo(333);
        assertThat(value.asInt(i -> i*3, 333)).isEqualTo(333);
        
        assertThat(value.asLong(333)).isEqualTo(333);
        assertThat(value.asLong(i -> i*3, 333)).isEqualTo(333);
        
        value = Value.of("cake");
        assertThat(value.asDouble(333)).isEqualTo(333d);
        assertThat(value.asDouble(i -> i*3, 333)).isEqualTo(333d);

        assertThat(Value.empty().value("fallback")).isEqualTo("fallback");
        assertThat(Value.of("false").value(Integer::parseInt, 333)).isEqualTo(333);
    }
    
    
    
    @Test
    public void unparsable() {
        assertThrows(Up.ParsableError.class, () -> Value.of("false").asInt());
        assertThrows(Up.ParsableError.class, () -> Value.of("false").asDouble());
    }
    
    @Ignore(value = "It is no longer illegal to use primitive types")
    @Test(expected = IllegalArgumentException.class)
    public void primitiveNotAllowedAsGeneric() {
        Value value = Value.of("4000");
        value.asOptional(int.class);
    }
    
    @Test
    public void asList() {
        //new ValueImpl(engine, new ListParsable("aa", "bb")).toList();
        assertThat(Value.of("aa", "bb").asList()).containsExactly("aa", "bb");
        assertThat(Value.of("aa", "bb").value()).isEqualTo("aa");
        
        assertThat(Value.of("single item").asList()).containsExactly("single item");
        assertThat(Value.of("1", "2", "33").asList(Integer.class)).containsExactly(1, 2, 33);
    }
    
    @Test
    public void asSet() {
        assertThat(Value.of("aa", "bb", "bb", "cc").asSet()).containsExactly("aa", "bb", "cc");
        
        assertThat(Value.of("single item").asSet()).containsExactly("single item");
        assertThat(Value.of("1","2", "2", "33").asSet(Integer.class)).containsExactly(1, 2, 33);
    }
    
    @Test
    public void iterator() {
        Value.of("single item").iterator();
    }
    
    @Test
    public void emptyList() {
        List<String> list = Value.of(((String)null)).asList();
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
    public void ifPresent() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Consumer<String> action = s -> ran.set(true);
        
        Value.empty().ifPresent(action);
        assertThat(ran.get()).isFalse(); // nothing is present
        
        Value.of("800").ifPresent(action);
        assertThat(ran.get()).isTrue();
    }
    
    @Test
    public void ifPresentThenMap() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Consumer<Integer> action = s -> ran.set(true);
        
        Value.of("800").ifPresent(Integer.class, action);
        assertThat(ran.get()).isTrue();
        
        assertThrows(Up.ParsableError.class, () -> Value.of("false").ifPresent(Integer.class, action));
    }
    
    @Test
    public void asEnum() {
        Value value = Value.of("A");
        assertThat(value.asEnum(LETTER.class)).isEqualTo(LETTER.A);
    }
    
    @Test
    public void asEnumList() {
        Value value = Value.of("A", "B");
        List<LETTER> list = value.asList(LETTER.class);
        
        //List<LETTER> list = ValueImpl.of(Parsable.of("A", "B")).toList(LETTER.class);
        assertThat(list).containsExactly(LETTER.A, LETTER.B);
    }
    
    enum LETTER { A,B }
}
