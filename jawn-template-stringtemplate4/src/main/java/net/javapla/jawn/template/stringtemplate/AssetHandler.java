package net.javapla.jawn.template.stringtemplate;

import java.nio.file.Path;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.annotation.Inject;

public class AssetHandler implements Route.Handler {

    private static final long serialVersionUID = -3366943711791220509L;
    private final DeploymentInfo info;

    @Inject
    public AssetHandler(DeploymentInfo info) {
        this.info = info;
    }
    
    @Override
    public Object handle(Context ctx) throws Exception {
        
        Path file = info.resolve(ctx.req().pathParam("file").value());
        System.out.println(file);
        
        
        return file;
    }

}
