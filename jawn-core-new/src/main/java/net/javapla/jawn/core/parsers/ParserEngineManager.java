package net.javapla.jawn.core.parsers;

import java.util.Set;

import net.javapla.jawn.core.MediaType;

public interface ParserEngineManager {

    /**
     * Returns a set of the registered body parser engine content types.
     *
     * @return the registered content types
     */
    Set<MediaType> getContentTypes();

    /**
     * Find the body parser engine for the given content type
     *
     * @param contentType
     *            The content type
     * @return The body parser engine, if found
     */
    ParserEngine getParserEngineForContentType(MediaType contentType);
}
