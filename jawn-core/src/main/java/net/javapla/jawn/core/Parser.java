package net.javapla.jawn.core;

import java.lang.reflect.Type;

/**
 * Parsing incoming messages/request body
 *
 */
public interface Parser {
    
    Object parse(Context ctx, Type type) throws Exception;

    interface ParserProvider {
        Parser get(MediaType type);
    }
}
