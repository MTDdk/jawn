package net.javapla.jawn.core;

import java.util.HashMap;
import java.util.Map;

public interface TemplateRenderer extends Renderer {
    
    String ENV_TEMPLATE_PATH_KEY = "views.path";
    
    String DEFAULT_TEMPLATE_PATH = "views";
    //String DEFAULT_TEMPLATE_DIR

    byte[] render(Context ctx, Template template) throws Exception;
    
    @Override
    default byte[] render(Context ctx, Object value) throws Exception {
        ctx.resp().rendererContentType(MediaType.HTML);
        return render(ctx, (Template) value);
    }
    
    
    public static class Template {
        
        public final String viewName;
        public final Map<String, Object> data;

        public Template(String view, Map<String, Object> data) {
            this.viewName = view;
            this.data = data;
        }
        
        public Template(String view) {
            this(view, new HashMap<>());
        }
        
    }
}
