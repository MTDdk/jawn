package net.javapla.jawn.core.parsers;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;

public class ParserEngineMangerTest {

    private static ParserEngineManager engine;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        engine = Guice.createInjector().getInstance(ParserEngineManager.class);
    }
        
    @Test
    public void registeredEngines() {
        assertThat(engine.getContentTypes()).containsAllIn(new MediaType[] {MediaType.XML,MediaType.JSON});
    }

    @Test
    public void jsonEngine() {
        //System.out.println(new ObjectMapper().writeValueAsString(new T("test")));
        
        ParserEngine json = engine.getParserEngineForContentType(MediaType.JSON);
        T t = json.invoke("{\"test\":\"test\"}".getBytes(), T.class);
        
        assertThat(t).isNotNull();
        assertThat(t.test).isEqualTo("test");
    }
    
    @Test
    public void jsonEngine_fail() {
        ParserEngine json = engine.getParserEngineForContentType(MediaType.JSON);
        
        assertThrows(Up.ParsableError.class, () -> json.invoke("{\"nope\":\"test\"}".getBytes(), T.class));
    }
    
    @Test
    public void jsonEngine_stream() {
        ParserEngine json = engine.getParserEngineForContentType(MediaType.JSON);
        T t = json.invoke(new ByteArrayInputStream("{\"test\":\"test\"}".getBytes()), T.class);
        
        assertThat(t).isNotNull();
        assertThat(t.test).isEqualTo("test");
    }
    
    @Test
    public void xmlEngine() {
        //System.out.println(new XmlMapper().writeValueAsString(new T("test")));
        
        ParserEngine xml = engine.getParserEngineForContentType(MediaType.XML);
        T t = xml.invoke("<T><test>test</test></T>".getBytes(), T.class);
        
        assertThat(t).isNotNull();
        assertThat(t.test).isEqualTo("test");
    }
    
    @Test
    public void xmlEngine_fail() {
        ParserEngine xml = engine.getParserEngineForContentType(MediaType.XML);
        assertThrows(Up.ParsableError.class, () -> xml.invoke("<T><nope>test</test></T>".getBytes(), T.class));
        assertThrows(Up.ParsableError.class, () -> xml.invoke("<T><nope>test</nope></T>".getBytes(), T.class));
        assertThrows(Up.ParsableError.class, () -> xml.invoke("<T><test>test</nope></T>".getBytes(), T.class));
        assertThrows(Up.ParsableError.class, () -> xml.invoke("<test>test</test>".getBytes(), T.class));
    }
    
    @Test
    public void xmlEngine_stream() {
        ParserEngine xml = engine.getParserEngineForContentType(MediaType.XML);
        T t = xml.invoke(new ByteArrayInputStream("<T><test>test</test></T>".getBytes()), T.class);
        
        assertThat(t).isNotNull();
        assertThat(t.test).isEqualTo("test");
    }
    
    @Test
    public void engines_stream_fail() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read()).thenThrow(IOException.class);
        
        
        ParserEngine xml = engine.getParserEngineForContentType(MediaType.XML);
        assertThrows(Up.ParsableError.class, () -> xml.invoke(stream, T.class));
        
        ParserEngine json = engine.getParserEngineForContentType(MediaType.JSON);
        assertThrows(Up.ParsableError.class, () -> json.invoke(stream, T.class));
    }
    
    static class T {
        public String test;
        public T() {}
        public T(String t) { test = t; }
    }
}
