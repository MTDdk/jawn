package net.javapla.jawn.core.parsers;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;

/**
 * Invoke the parser and get back a Java object populated
 * with the content of this request.
 * 
 * MUST BE THREAD SAFE TO CALL!
 * 
 * @param reader The context
 * @param clazz The class we expect
 * @return The object instance populated with all values from raw request
 * @author MTD
 */
public interface ParserEngine {

    interface Parsable extends Iterable<Parsable> {
        long length();
        byte[] bytes() throws IOException;
        //String text() throws IOException;
        
        @Override
        default Iterator<ParserEngine.Parsable> iterator() {
            return Collections.emptyIterator();
        }
    }

    //<T> T invoke(Reader reader, Class<T> clazz) throws Up.ParsableError;
    
    //<T> T invoke(InputStream stream, Class<T> clazz) throws Up.ParsableError;
    
    //<T> T invoke(byte[] arr, Class<T> clazz) throws Up.ParsableError;
    
    //<T> T invoke(Parsable parsable, Class<T> clazz) throws Up.ParsableError;
//    <T> T invoke(StreamParsable parsable, Class<T> clazz) throws Up.ParsableError;
//    <T> T invoke(ParameterParsable parsable, Class<T> clazz) throws Up.ParsableError;
    
    <T> T invoke(Parsable parsable, Class<T> type) throws Up.ParsableError;
    /*default <T> T invoke(Parsable parsable, Class<T> type, ParameterizedType holder) throws Up.ParsableError {
        return invoke(parsable, type);
    }*/
    
    /*default <T> T invoke(Parsable parsable, Class<T> type) throws Up.ParsableError {
        return invoke(parsable, type, null);
    }*/
    
    /*@SuppressWarnings("unchecked")
    default <T> T invoke(StreamParsable parsable, ParameterizedType type) throws Up.ParsableError {
        return invoke(parsable, (Class<T>) type.getRawType());
    }
    @SuppressWarnings("unchecked")
    default <T> T invoke(ParameterParsable parsable, ParameterizedType type) throws Up.ParsableError {
        return invoke(parsable, (Class<T>) type.getRawType());
    }*/
    
    /**
     * The content type this BodyParserEngine can handle
     * 
     * MUST BE THREAD SAFE TO CALL!
     * 
     * @return the content type. this parser can handle - eg. "application/json"
     */
    MediaType[] getContentType();
}
