package net.javapla.jawn.core.renderers;

import java.util.Set;
import java.util.function.Consumer;

import net.javapla.jawn.core.Err;
import net.javapla.jawn.core.MediaType;

/**
 * Template engine manager. Has a number of built in template engines, and
 * allows registering custom template engines by registering explicit bindings
 * of things that implement TemplateEngine.
 */
public interface RendererEngineOrchestrator {

    /**
     * Returns a set of the registered template engine content types.
     *
     * @return the registered content types
     */
    Set<MediaType> getContentTypes();

    /**
     * Find the template engine for the given content type
     *
     * @param contentType
     *            The content type
     * @return The template engine, if found
     */
    void getRendererEngineForContentType(MediaType contentType, Consumer<RendererEngine> callback) throws Err.BadMediaType;

}