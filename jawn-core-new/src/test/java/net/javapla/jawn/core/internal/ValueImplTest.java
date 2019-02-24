package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;

import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.parsers.Parsable;
import net.javapla.jawn.core.parsers.ParserEngineManager;

public class ValueImplTest {

    @Test
    public void asString() {
        Value value = ValueImpl.of(Parsable.of("4000"));//new ValueImpl(engine, new StringParsable("4000"));
        
        assertThat(value.to(String.class)).isEqualTo("4000");
        
        String result = value.value();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo("4000");
    }
    
    @Test
    public void asOptional() {
        Value value = ValueImpl.of(Parsable.of("4000"));
        Optional<String> optional = value.toOptional(String.class);
        
        assertThat(optional).isInstanceOf(Optional.class);
        assertThat(optional.get()).isEqualTo("4000");
    }
    
    @Test
    public void emptyOptional() {
        Value value = ValueImpl.of(Parsable.of((String)null));
        assertThat(value.toOptional().isPresent()).isFalse();
        
        value = ValueImpl.of(Parsable.of(""));
        assertThat(value.toOptional().isPresent()).isFalse();
    }

    @Test
    public void asDouble() {
        Value value = ValueImpl.of(Parsable.of("4000.3"));
        
        assertThat(value.doubleValue()).isEqualTo(4000.3);
    }
    
    @Test
    public void withFallback() {
        Value value = ValueImpl.of(Parsable.of("4000.3"));
        
        assertThat(value.intValue(333)).isEqualTo(333);
    }
    
    @Test(expected = Up.ParsableError.class)
    public void unparsable() {
        Value value = ValueImpl.of(Parsable.of("false"));
        value.intValue();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void primitiveNotAllowedAsGeneric() {
        Value value = ValueImpl.of(Parsable.of("4000"));
        value.toOptional(int.class);
    }
    
    @Test
    public void asStringList() {
        List<String> list = ValueImpl.of(Parsable.of("aa", "bb")).toList();//new ValueImpl(engine, new ListParsable("aa", "bb")).toList();
        assertThat(list).containsExactly("aa", "bb");
    }
    
    @Test
    public void asStringSet() {
        Set<String> set = ValueImpl.of(Parsable.of("aa", "bb", "bb", "cc")).toSet();
        assertThat(set).containsExactly("aa", "bb", "cc");
    }
    
    @Test
    public void emptyList() {
        List<String> list = ValueImpl.of(Parsable.of((String)null)).toList();
        assertThat(list).isEmpty();
    }
    
    @Test
    public void mapString() {
        String otherValue = "true";
        boolean result = ValueImpl.of(Parsable.of("true")).map(otherValue::equals).orElse(false);
        assertThat(result).isTrue();
        
        result = ValueImpl.of(Parsable.of("false")).map(otherValue::equals).orElse(false);
        assertThat(result).isFalse();
    }
    
    @Test
    public void convertAndMap() {
        long lastModified = 1080;
        boolean result = ValueImpl.of( Parsable.of("1080")).map(Long.class, modifiedSince -> lastModified <= modifiedSince).orElse(false);
        assertThat(result).isTrue();
        
        result = ValueImpl.of(Parsable.of("640")).map(Long.class, modifiedSince -> lastModified <= modifiedSince).orElse(false);
        assertThat(result).isFalse();
    }
    
    @Test
    public void asEnum() {
        Value value = ValueImpl.of(Parsable.of("A"));
        assertThat(value.toEnum(LETTER.class)).isEqualTo(LETTER.A);
    }
    
    @Test
    public void asEnumList() {
        List<LETTER> list = ValueImpl.of(Parsable.of("A", "B")).toList(LETTER.class);
        assertThat(list).containsExactly(LETTER.A, LETTER.B);
    }
    
    enum LETTER { A,B }
}
