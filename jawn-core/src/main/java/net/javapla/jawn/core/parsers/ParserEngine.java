package net.javapla.jawn.core.parsers;

import java.lang.reflect.Type;

import net.javapla.jawn.core.Context;
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

    //<T> T invoke(Parsable parsable, Class<T> type) throws Up.ParsableError;
    
    //<T> T invoke(byte[] arr, Class<T> type) throws Up.ParsableError;
    //<T> T invoke(InputStream stream, Class<T> type) throws Up.ParsableError;
    Object invoke(Context context, Type type) throws Up.ParsableError;
    
    /**
     * The content type this BodyParserEngine can handle
     * 
     * MUST BE THREAD SAFE TO CALL!
     * 
     * @return the content type. this parser can handle - eg. "application/json"
     */
    default MediaType[] getContentType() {
        return new MediaType[0];
    }
    
    
    ParserEngine UNSUPPORTED_MEDIA_TYPE = (ctx, type) -> {
        throw Up.ParsableError.here(ctx.req().header("Content-Type").value());
    };
}
