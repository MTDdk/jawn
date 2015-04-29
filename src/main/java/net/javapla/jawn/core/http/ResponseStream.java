package net.javapla.jawn.core.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Wrapper for encapsulation
 * 
 * @author MTD
 *
 */
public interface ResponseStream {

    public OutputStream getOutputStream() throws IOException;
    
    public Writer getWriter() throws IOException;
}
