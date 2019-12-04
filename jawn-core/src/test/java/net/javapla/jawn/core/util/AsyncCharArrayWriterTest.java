package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class AsyncCharArrayWriterTest {

    @Test
    public void illegalArgumentForConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new AsyncCharArrayWriter(-1));
        assertThrows(IllegalArgumentException.class, () -> new AsyncCharArrayWriter(-100));
    }

    @Test
    public void appendString() throws IOException {
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            writer.append("testing");
            
            assertThat(writer.toString()).isEqualTo("testing");
        }
    }
    
    @Test
    public void appendChar() throws IOException {
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            writer
                .append('t')
                .append('e')
                .append('s')
                .append('t')
                .append('i')
                .append('n')
                .append('g');
                
            assertThat(writer.toString()).isEqualTo("testing");
        }
    }
    
    @Test
    public void toCharArray() throws IOException {
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            writer.append("testing");
            
            assertThat(writer.toCharArray()).isEqualTo("testing".toCharArray());
        }
    }
    
    @Test
    public void toByteBuffer() throws IOException {
        String s = "testingæøå";
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            writer.append(s);
            
            assertThat(writer.toByteBuffer(StandardCharsets.UTF_8)).isNotEqualTo(ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_16)));
            assertThat(writer.toByteBuffer(StandardCharsets.UTF_8)).isEqualTo(ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)));
        }
    }
    
    @Test
    public void writeArray() throws IOException {
        char[] arr = "Is testing for everyone?".toCharArray();
        int offset = "Is ".length();
        int length = "testing".length();
        
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            writer.write(arr, offset, length);
            
            assertThat(writer.toCharBuffer()).isEqualTo(CharBuffer.wrap("testing"));
        }
    }
    
    @Test
    public void writeArray_outOfBounds() throws IOException {
        char[] arr = "Is testing for everyone?".toCharArray();
        int offset = "Is ".length();
        
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(arr, offset, arr.length));
            
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(arr, "Is testing ".length(), arr.length));
        }
    }
    
    @Test
    public void writeArray_noLength_should_not_writeAnything() throws IOException {
        char[] arr = "testing".toCharArray();
        
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            writer.write(arr, 0, 0);
            
            assertThat(writer.size()).isEqualTo(0);
        }
    }
    
    @Test
    public void hasResized() {
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter(1)) {
            
            assertThat(writer.buf).hasLength(1);
            
            writer.write('1');
            assertThat(writer.size()).isEqualTo(1);
            
            writer.write('2');
            assertThat(writer.size()).isEqualTo(2);
            
            writer.write('3');
            assertThat(writer.size()).isEqualTo(3);
            
            assertThat(writer.buf.length).isGreaterThan(3);
        }
    }
    
    @Test
    public void flush_should_not_doAnything() throws IOException {
        try (AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            
            writer.write("more tests");
            assertThat(writer.size()).isEqualTo(10);
            
            writer.flush();
            assertThat(writer.size()).isEqualTo(10);
        }
    }
    
}
