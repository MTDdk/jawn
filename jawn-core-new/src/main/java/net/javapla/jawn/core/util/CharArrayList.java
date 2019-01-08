package net.javapla.jawn.core.util;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Non-synchronised version of {@link CharArrayWriter} 
 *
 */
public final class CharArrayList {
    /**
     * The buffer where data is stored.
     */
    protected char buf[];

    /**
     * The number of chars in the buffer.
     */
    protected int count;

    /**
     * Creates a new CharArrayWriter.
     */
    public CharArrayList() {
        this(32);
    }

    /**
     * Creates a new CharArrayWriter with the specified initial size.
     *
     * @param initialSize  an int specifying the initial buffer size.
     * @exception IllegalArgumentException if initialSize is negative
     */
    public CharArrayList(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: "
                                               + initialSize);
        }
        buf = new char[initialSize];
    }
    
    /**
     * Writes a character to the buffer.
     */
    public void write(int c) {
        int newcount = count + 1;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        buf[count] = (char)c;
        count = newcount;
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
     * Converts input data to a string.
     * @return the string.
     */
    @Override
    public String toString() {
        return new String(buf, 0, count);
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
