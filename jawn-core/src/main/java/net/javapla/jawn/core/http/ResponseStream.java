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
public interface ResponseStream extends AutoCloseable {

    OutputStream getOutputStream() throws IOException;
    
    Writer getWriter() throws IOException;
}
