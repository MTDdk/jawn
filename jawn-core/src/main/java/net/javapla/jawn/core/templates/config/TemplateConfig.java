package net.javapla.jawn.core.templates.config;

/**
 * 
 * @author MTD
 * @param <T> Type of the configuration exposed to the user 
 *          and then used by the template renderer to configure it.
 */
public interface TemplateConfig<T> {
    /**
     * Called by framework during initialization.
     * @param config The configuration of the template renderer. Used by the user to tinker with the workings of the renderer.
     */
    void init(final T config);
}
