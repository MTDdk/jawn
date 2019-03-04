package net.javapla.jawn.core.parsers;

import static com.google.common.truth.Truth.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;

import net.javapla.jawn.core.MediaType;

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
    public void xmlEngine() {
        //System.out.println(new XmlMapper().writeValueAsString(new T("test")));
        
        ParserEngine xml = engine.getParserEngineForContentType(MediaType.XML);
        T t = xml.invoke("<T><test>test</test></T>".getBytes(), T.class);
        
        assertThat(t).isNotNull();
        assertThat(t.test).isEqualTo("test");
    }
    
    static class T {
        public String test;
        public T() {}
        public T(String t) { test = t; }
    }
}
