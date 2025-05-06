package net.javapla.jawn.core;

public interface TemplateRenderer extends Renderer {
    
    String ENV_RESOURCES_PATH_LOCATION = "resources.location";
    
    // if we do not want to call it "views"
    String ENV_TEMPLATE_PATH_NAME = "views.path";
    
    String DEFAULT_TEMPLATE_NAME = "views";
    //String DEFAULT_TEMPLATE_DIR

    byte[] render(Context ctx, View template) throws Exception;
    
    @Override
    default byte[] render(Context ctx, Object value) throws Exception {
        //ctx.resp().rendererContentType(MediaType.HTML);
        ctx.resp().contentType(MediaType.HTML);
        return render(ctx, (View) value);
    }
}
