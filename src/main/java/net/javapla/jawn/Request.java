package net.javapla.jawn;

import java.io.IOException;
import java.io.InputStream;

import net.javapla.jawn.exceptions.MediaTypeException;
import net.javapla.jawn.exceptions.ParsableException;

public interface Request {
    
    /**
     * Internal contract goes here.
     * <p>
     * Not visible for users
     */
    interface Impl extends Request {
        
    }

    byte[] asBytes() throws IOException;

    InputStream asStream() throws IOException;

    String asText() throws ParsableException;

    <T> T fromJson(Class<T> clazz) throws ParsableException, MediaTypeException;

}
