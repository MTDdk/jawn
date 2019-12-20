package net.javapla.jawn.core.internal;

import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.Assets;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.renderers.template.TemplateRendererEngine;

public abstract class AssetRouter {
    private final static Logger logger = LoggerFactory.getLogger(AssetRouter.class.getSimpleName());
    
    private AssetRouter() {}
    
    /*
     * This class should be a part of the template-module
     * You probably do not want to serve static files without such a template-module anyways
     * 
     * Like the MvcRouter, this should spew out a bunch of routes for each folder within webapp/
     * - i.e.: Route(/js/*),Route(/css/*),Route(/img/*)
     */
    
    public static List<Route.Builder> assets(final DeploymentInfo deploymentInfo, final Assets.Impl assets) {
        Set<String> paths = findAssetPaths(deploymentInfo);
        logger.debug("Letting the server take care of providing resources from: {}", paths);
        
        
        
        return paths
            .stream()
            .map(path -> {
                
                // We assume if the path contains a dot '.', it is a file and not a folder
                if (path.indexOf('.') > 0) {
                    return new Route.Builder(HttpMethod.GET)
                        .path(path)
                        .handler(
                            new AssetHandler(deploymentInfo)
                                .etag(assets.etag)
                                .lastModified(assets.lastModified)
                                .maxAge(assets.maxAge)
                        );
                }
                
                // This should be a folder then
                return new Route.Builder(HttpMethod.GET)
                                .path(path + "/{file: .*}")
                                .handler(new AssetHandler(deploymentInfo)
                                    .etag(assets.etag)
                                    .lastModified(assets.lastModified)
                                    .maxAge(assets.maxAge)
                                );
            })
            .collect(Collectors.toList());
    }

    private static Set<String> findAssetPaths(final DeploymentInfo deploymentInfo) throws Up.IO {
        final Set<String> assets = new TreeSet<String>(); // Actually sorts the paths, which is not appreciated by the application and not even used anywhere
        
        
        List<String> collect = null;
        try {
            collect = deploymentInfo.listResources("").collect(Collectors.toList());
        } catch (NoSuchFileException e) { }

        
        if (collect == null || collect.isEmpty()) { 
            // Whenever this is empty it might just be because someone forgot to add the 'webapp' folder to the distribution
            // OR the framework is used without the need for serving files (such as views).
            logger.info(AssetRouter.class.getName() + " did not find any files in webapp - not serving any files then");
            return assets;
        }
        
        
        Set<String> resourcePaths = new TreeSet<>(collect);//servletContext.getResourcePaths("/");
        resourcePaths.removeIf( path -> path.contains("-INF") || path.contains("-inf")); // Let other handlers deal with folders that do not reside in the WEB-INF or META-INF
        resourcePaths.removeIf( path -> path.charAt(0) == '.');
        
        // We still need to also remove the views folder from being processed by other handlers
        resourcePaths.removeIf( path -> path.contains(TemplateRendererEngine.TEMPLATES_FOLDER)); // TODO use the fact that the templates folder is handled by DeploymentInfo
        
        
        // Add the remaining paths to exclusions
        for (String path : resourcePaths) {
            // add leading slash
            if (path.charAt(0) != '/')
                path = '/' + path;
            
            // remove trailing slash
            if (path.charAt(path.length()-1) == '/')
                path = path.substring(0, path.length()-1);
            assets.add(path);
        }
        
        return assets;
    }
}
