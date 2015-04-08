package net.javapla.jawn.parsers;

import java.util.Set;

public interface ParserEngineManager {

    /**
     * Returns a set of the registered body parser engine content types.
     *
     * @return the registered content types
     */
    Set<String> getContentTypes();

    /**
     * Find the body parser engine for the given content type
     *
     * @param contentType
     *            The content type
     * @return The body parser engine, if found
     */
    ParserEngine getParserEngineForContentType(String contentType);
}
