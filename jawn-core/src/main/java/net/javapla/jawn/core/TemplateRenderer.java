package net.javapla.jawn.core;

import java.util.Map;

public interface TemplateRenderer extends Renderer {

    byte[] render(Context ctx, Template template);
    
    @Override
    default byte[] render(Context ctx, Object value) throws Exception {
        ctx.resp().rendererContentType(MediaType.HTML);
        return render(ctx, (Template) value);
    }
    
    public static class Template {
        
        public final String view;
        public final Map<String, Object> data;

        public Template(String view, Map<String, Object> data) {
            this.view = view;
            this.data = data;
        }
        
    }
}
