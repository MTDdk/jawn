package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class AsyncBufferedReaderTest {

    @Test
    public void illegalSize() {
        assertThrows(IllegalArgumentException.class, () -> new AsyncBufferedReader(new StringReader(""), 0));
        assertThrows(IllegalArgumentException.class, () -> new AsyncBufferedReader(new StringReader(""), -2));
    }
    
    @Test
    public void readIntoArray() throws IOException {
        String s = "testing string";
        char[] buffer = new char[10];
        
        try (AsyncBufferedReader reader = new AsyncBufferedReader(new StringReader(s), s.length()/2)) {
            reader.read(buffer, 0, 10);
        }
        
        assertThat(buffer).isEqualTo(s.substring(0, 10).toCharArray());
    }
    
    @Test
    public void read_should_not_bePossibleAfterClose() throws IOException {
        String s = "testing string";
        char[] buffer = new char[s.length()];
        
        AsyncBufferedReader reader = new AsyncBufferedReader(new StringReader(s));
        
        reader.read(buffer);
        reader.close();
        
        assertThat(buffer).isEqualTo(s.toCharArray());
        assertThrows(IOException.class, () -> reader.read());
    }

    @Test
    public void mark_should_beSupported() throws IOException {
        try (Reader r = new AsyncBufferedReader(new StringReader(""))) {
            assertThat(r.markSupported()).isTrue();
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void markInvalid() throws IOException {
        Reader r = new AsyncBufferedReader(new StringReader("123456789"));
        
        r.mark(-100);
    }
    
    @Test
    public void markReset() throws IOException {
        Reader r = new AsyncBufferedReader(new StringReader("123456789"));
        
        assertThat((char)r.read()).isEqualTo('1');
        assertThat((char)r.read()).isEqualTo('2');
        
        r.mark(2);
        
        assertThat((char)r.read()).isEqualTo('3');
        assertThat((char)r.read()).isEqualTo('4');
        assertThat((char)r.read()).isEqualTo('5');
        assertThat((char)r.read()).isEqualTo('6');
        
        r.reset();
        
        assertThat((char)r.read()).isEqualTo('3');
        assertThat((char)r.read()).isEqualTo('4');
        assertThat((char)r.read()).isEqualTo('5');
        assertThat((char)r.read()).isEqualTo('6');
        assertThat((char)r.read()).isEqualTo('7');
        assertThat((char)r.read()).isEqualTo('8');
        assertThat((char)r.read()).isEqualTo('9');
        
        r.close();
    }
    
    @Test(expected = IOException.class)
    public void resetInvalid() throws IOException {
        Reader r = new AsyncBufferedReader(new StringReader("123456789"));
        r.reset();
    }
    
    @Test
    public void readLine_with_invalidatedMark() throws IOException {
        AsyncBufferedReader r = new AsyncBufferedReader(new StringReader("12345\n6789"));
        
        assertThat(r.readLine()).isEqualTo("12345");
        r.mark(0);
        assertThat(r.readLine()).isEqualTo("6789");
        
        r.close();
    }
    
    @Test
    public void readLine_with_validMark() throws IOException {
        AsyncBufferedReader r = new AsyncBufferedReader(new StringReader("12345\n6789"));
        
        assertThat(r.readLine()).isEqualTo("12345");
        r.mark(20);
        assertThat(r.readLine()).isEqualTo("6789");
        
        r.close();
    }
    
    @Test
    public void readLine_with_reallocatedBuffer() throws IOException {
        AsyncBufferedReader r = new AsyncBufferedReader(new StringReader("12345\n6789"), 15);
        
        assertThat(r.readLine()).isEqualTo("12345");
        r.mark(20);
        assertThat(r.readLine()).isEqualTo("6789");
        
        r.close();
    }
}
