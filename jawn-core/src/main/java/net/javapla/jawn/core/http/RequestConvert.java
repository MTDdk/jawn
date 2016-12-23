package net.javapla.jawn.core.http;

import java.io.IOException;
import java.io.InputStream;

import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.exceptions.ParsableException;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.parsers.ParserEngineManager;

public interface RequestConvert {
    
    byte[] asBytes() throws IOException;

    InputStream asStream() throws IOException;

    String asText() throws ParsableException;

    /**
     * This will try to parse the request body nicely into an object.
     * It determines the parser based on the request type.
     * 
     * You can register your own parsers for other request types, if needed.
     * Take a look at: {@link ParserEngine} and {@link ParserEngineManager}
     * 
     * @param clazz A representation of the expected body
     * @return The parsed request object, or <code>throws</code> if the body could not be correctly deserialized,
     *         or the media type was incorrect.
     * @throws ParsableException If the parsing from the given content type to class failed
     * @throws MediaTypeException If the media type of the request was not specified
     */
    <T> T parseBody(Class<T> clazz) throws ParsableException, MediaTypeException;

}
