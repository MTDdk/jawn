package net.javapla.jawn.core;

public interface TemplateRenderer extends Renderer {
    
    String ENV_RESOURCES_PATH_LOCATION = "resources.location";
    String DEFAULT_RESOURCES_PATH_LOCATION = "webapp";
    
    // if we do not want to call it "views"
    String ENV_TEMPLATE_FOLDER_NAME = "views.name";
    String DEFAULT_TEMPLATE_FOLDER_NAME = "views";

    byte[] render(Context ctx, View template) throws Exception;
    
    @Override
    default byte[] render(Context ctx, Object value) throws Exception {
        ctx.resp().rendererContentType(MediaType.HTML);
        //ctx.resp().contentType(MediaType.HTML);
        return render(ctx, (View) value);
    }
}
