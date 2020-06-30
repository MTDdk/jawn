package net.javapla.jawn.core.renderers.template;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.renderers.RendererEngine;

public interface TemplateRendererEngine extends RendererEngine {
    String TEMPLATES_FOLDER = System.getProperty("resources.templates.folder", "views");//"WEB-INF/views/"); //TODO use DeploymentInfo instead
    String LAYOUT_DEFAULT = "index.html"; //TODO move to jawn_defaults.properties
    
    @Override
    default void invoke(Context context, Object renderable) throws Exception {
        if (renderable instanceof View) {
            View v = (View)renderable;
            invoke(context, v);
        }
    }
    
    void invoke(Context context, View viewable);
    
    String invoke(View viewable);

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
    
}
