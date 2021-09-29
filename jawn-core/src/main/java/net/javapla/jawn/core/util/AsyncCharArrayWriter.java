package net.javapla.jawn.core.util;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Non-synchronised version of {@link CharArrayWriter} 
 *
 */
public final class AsyncCharArrayWriter extends Writer {
    /**
     * The buffer where data is stored.
     */
    protected char[] buf;

    /**
     * The number of chars in the buffer.
     */
    protected int count;

    /**
     * Creates a new CharArrayWriter.
     */
    public AsyncCharArrayWriter() {
        this(32_768); // 32M
    }

    /**
     * Creates a new CharArrayWriter with the specified initial size.
     *
     * @param initialSize  an int specifying the initial buffer size.
     * @exception IllegalArgumentException if initialSize is negative
     */
    public AsyncCharArrayWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: " + initialSize);
        }
        buf = new char[initialSize];
    }
    
    /**
     * Writes a character to the buffer.
     */
    public void write(int c) {
        int newcount = count + 1;
        ensureBuffer(newcount);
        
        buf[count] = (char)c;
        count = newcount;
    }
    
    @Override
    public void write(char[] c, int off, int len) throws IndexOutOfBoundsException {
        if ((off < 0) || (off > c.length) || (len < 0) ||
            ((off + len) > c.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newcount = count + len;
        ensureBuffer(newcount);
        
        System.arraycopy(c, off, buf, count, len);
        count = newcount;
    }
    
    @Override
    public void write(String str, int off, int len) {
        int newcount = count + len;
        ensureBuffer(newcount);
        
        str.getChars(off, off + len, buf, count);
        count = newcount;
    }
    
    @Override
    public void write(String str) {
        write(str, 0, str.length());
    }
    
    private void ensureBuffer(int newcount) {
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
    }
    
    /**
     * Resets the buffer so that you can use it again without
     * throwing away the already allocated buffer.
     */
    public void reset() {
        count = 0;
    }
    
    /**
     * Returns the current size of the buffer.
     *
     * @return an int representing the current size of the buffer.
     */
    public int size() {
        return count;
    }
    
    /**
     * Returns a copy of the input data.
     *
     * @return an array of chars copied from the input data.
     */
    public char[] toCharArray() {
        return Arrays.copyOf(buf, count);
    }
    
    /**
     * Writes the contents of the buffer to another character stream.
     *
     * @param out       the output stream to write to
     * @throws IOException If an I/O error occurs.
     */
    public void writeTo(Writer out) throws IOException {
        out.write(buf, 0, count);
    }
    
    /**
     * Converts input data to a string.
     * @return the string.
     */
    @Override
    public String toString() {
        return new String(buf, 0, count);
    }
    
    @Override
    public void flush() {}

    @Override
    public void close() {}
    
    public CharBuffer toCharBuffer() {
        return CharBuffer.wrap(buf, 0, count);
    }
    
    public ByteBuffer toByteBuffer(final Charset cs) {
        return cs.encode(toCharBuffer());
    }

    public byte[] getBytes(final Charset cs) {
        // in part the same as new String(buf, 0, count)
        /*byte[] ret = new byte[count];
        if (compress(buf, 0, ret, 0, count) == count) {
            return ret; // missing the Charset conversion so we cannot do any non-String-initialisation
        }*/
        
        // the most accurate according to Computer Science
        return new String(buf, 0, count).getBytes(cs);
    }
    
    /**
     * @see {@link StringUTF16#compress(char[], int, byte[], int, int)}
     */
    /*public static int compress(char[] src, int srcOff, byte[] dst, int dstOff, int len) {
        for (int i = 0; i < len; i++) {
            char c = src[srcOff];
            if (c > 0xFF) {
                len = 0;
                break;
            }
            dst[dstOff] = (byte)c;
            srcOff++;
            dstOff++;
        }
        return len;
    }*/
    
}
