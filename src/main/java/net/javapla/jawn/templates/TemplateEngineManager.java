package net.javapla.jawn.templates;

import java.util.Set;

/**
 * Template engine manager. Has a number of built in template engines, and
 * allows registering custom template engines by registering explicit bindings
 * of things that implement TemplateEngine.
 */
public interface TemplateEngineManager {

    /**
     * Returns a set of the registered template engine content types.
     *
     * @return the registered content types
     */
    Set<String> getContentTypes();

    /**
     * Find the template engine for the given content type
     *
     * @param contentType
     *            The content type
     * @return The template engine, if found
     */
    TemplateEngine getTemplateEngineForContentType(String contentType);

}