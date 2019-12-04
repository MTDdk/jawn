package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class StreamUtilTest {

    @Test
    public void copyStreams() throws IOException {
        String s = "test string";
        
        ByteArrayInputStream input = new ByteArrayInputStream(s.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        StreamUtil.copy(input, output);
        
        assertThat(output.toString()).isEqualTo(s);
    }

    @Test
    public void readerToString() throws IOException {
        StringReader reader = new StringReader("test string");
        
        assertThat(StreamUtil.read(reader).getBytes()).isEqualTo("test string".getBytes());
    }
}
