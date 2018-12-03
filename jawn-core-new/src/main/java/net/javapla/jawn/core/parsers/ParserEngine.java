package net.javapla.jawn.core.parsers;

import java.io.InputStream;
import java.io.Reader;

import net.javapla.jawn.core.Err;
import net.javapla.jawn.core.MediaType;

/**
 * 
 * @author MTD
 *
 */
public interface ParserEngine {

    /**
     * Invoke the parser and get back a Java object populated
     * with the content of this request.
     * 
     * MUST BE THREAD SAFE TO CALL!
     * 
     * @param reader The context
     * @param clazz The class we expect
     * @return The object instance populated with all values from raw request
     */
    <T> T invoke(Reader reader, Class<T> clazz) throws Err.ParsableError;
    
    <T> T invoke(InputStream stream, Class<T> clazz) throws Err.ParsableError;
    
    <T> T invoke(byte[] arr, Class<T> clazz) throws Err.ParsableError;
    
    /**
     * The content type this BodyParserEngine can handle
     * 
     * MUST BE THREAD SAFE TO CALL!
     * 
     * @return the content type. this parser can handle - eg. "application/json"
     */
    MediaType getContentType();
}
