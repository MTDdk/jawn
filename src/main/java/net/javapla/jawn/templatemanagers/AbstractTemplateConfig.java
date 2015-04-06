package net.javapla.jawn.templatemanagers;

import com.google.inject.Injector;

/**
 * 
 * @author MTD
 * @param <T> Type of the configuration exposed to the user 
 *          and then used by the template renderer to configure it.
 */
public abstract class AbstractTemplateConfig<T> {
    
    /**
     * Called by framework during initialization.
     * @param config The configuration of the template renderer. Used by the user to tinker with the workings of the renderer.
     */
    public abstract void init(final T config);
    
    /**
     * Injects user tags with members
     *
     * @param injector user tags with members using this injector.
     */
    public void inject(Injector injector) {}
}
