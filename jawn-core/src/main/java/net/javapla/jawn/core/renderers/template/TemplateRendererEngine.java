package net.javapla.jawn.core.renderers.template;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.renderers.RendererEngine;

public interface TemplateRendererEngine<T> extends RendererEngine {
    String TEMPLATES_FOLDER = System.getProperty("resources.templates.folder", "views");//"WEB-INF/views/"); //TODO move to jawn_defaults.properties
    String LAYOUT_DEFAULT = "index.html"; //TODO move to jawn_defaults.properties
    
    @Override
    default void invoke(Context context, Object renderable) throws Exception {
        if (renderable instanceof View) {
            View v = (View)renderable;
            invoke(context, v);
        }
    }
    
    void invoke(Context context, View viewable);

    /**
     * Let the template engine handle lookups of templates and in effect also caching hereof.
     * @param templatePath
     * @return
     */
    T readTemplate(String templatePath);
    
    /**
     * For instance returns .st, .ftl.html, or .ftl.json.
     * <p>
     * Or anything else. To display error messages in a nice way...
     * <p>
     * But Gson for instance does not use a template to render stuff. Therefore
     * it will return null
     * 
     * @return
     *      name of suffix or null if engine is not using a template on disk.
     */
    String getSuffixOfTemplatingEngine();
    
    /**
     * When using caches we want to be able to clone the cached value, so
     * nothing gets carried over when re-using the template
     * <p>
     * If the templates are re-usable out of the box, then no need to
     * override this
     * @param 
     *      cloneThis the template to be cloned
     * @return 
     *      the cloned template or simply <code>cloneThis</code> if nothing needs
     *      to be altered
     * 
     */
    default T clone(T cloneThis) { return cloneThis; } 
}
