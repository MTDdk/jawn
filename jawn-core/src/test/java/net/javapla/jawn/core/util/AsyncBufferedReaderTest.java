package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

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
}
