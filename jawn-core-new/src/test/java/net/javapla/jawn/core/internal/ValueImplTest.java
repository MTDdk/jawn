package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;

import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;

import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.internal.parsers.StringParsable;
import net.javapla.jawn.core.parsers.ParserEngineManager;

public class ValueImplTest {

    private static ParserEngineManager engine;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        engine = Guice.createInjector().getInstance(ParserEngineManager.class);
    }

    

    @Test
    public void asString() {
        Value value = new ValueImpl(engine, new StringParsable("4000"));
        
        assertThat(value.to(String.class)).isEqualTo("4000");
        
        String result = value.value();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo("4000");
    }
    
    @Test
    public void asOptional() {
        Value value = new ValueImpl(engine, new StringParsable("4000"));
        Optional<String> optional = value.toOptional(String.class);
        
        assertThat(optional).isInstanceOf(Optional.class);
        assertThat(optional.get()).isEqualTo("4000");
    }
    
    @Test
    public void emptyOptional() {
        Value value = new ValueImpl(engine, new StringParsable(null));
        assertThat(value.toOptional().isPresent()).isFalse();
        
        value = new ValueImpl(engine, new StringParsable(""));
        assertThat(value.toOptional().isPresent()).isFalse();
    }

}
