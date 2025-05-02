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
    
    
    public static record Template(String view, Map<String, Object> data) {
        // data = view model
        public Template(String view) {
            this(view, new HashMap<>());
        }
        
        public Template put(String key, Object value) {
            data.put(key, value);
            return this;
        }
    }
}
