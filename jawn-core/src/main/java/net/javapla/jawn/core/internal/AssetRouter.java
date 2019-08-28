package net.javapla.jawn.core.internal;

import java.io.File;
import java.util.Arrays;
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

public class AssetRouter {
    private final static Logger logger = LoggerFactory.getLogger(AssetRouter.class.getSimpleName());
    
    /*
     * This class should be a part of the template-module
     * You probably do not want to serve static files without such a template-module anyways
     * 
     * Like the MvcRouter, this should spew out a bunch of routes for each folder within webapp/
     * - i.e.: Route(/js/*),Route(/css/*),Route(/img/*)
     */
    
    public static List<Route.Builder> assets(final DeploymentInfo deploymentInfo, final Assets.Impl assets) {
        Set<String> paths = findExclusionPaths(deploymentInfo);
        logger.debug("Letting the server take care of providing resources from: {}", paths);
        
        return paths
            .stream()
            .map(path -> new Route.Builder(HttpMethod.GET)
                                .path(path + "/{file: .*}")
                                .handler(new AssetHandler(deploymentInfo)
                                    .etag(assets.etag)
                                    .lastModified(assets.lastModified)
                                    .maxAge(assets.maxAge)
                                )
            )
            .collect(Collectors.toList());
    }

    private static Set<String> findExclusionPaths(final DeploymentInfo deploymentInfo) throws Up.IO {
        Set<String> exclusions = new TreeSet<String>(); // Actually sorts the paths, which is not appreciated and not even used anywhere
        
        // Let other handlers deal with folders that do not reside in the WEB-INF or META-INF
        List<String> collect = null;
        File webapp = deploymentInfo.getRealPath("").toFile();
        String[] paths = webapp.list();
        if (webapp.exists() && paths != null)
            collect = Arrays.asList(paths);
        
        if (webapp.exists() && !webapp.canRead()) {
            // It means that the server cannot read files at all
            throw new Up.IO( AssetRouter.class.getName() + " cannot read files. Reason is unknown");
        } else if (!webapp.exists() || collect == null || collect.isEmpty()) {
            // Whenever this is empty it might just be because someone forgot to add the 'webapp' folder to the distribution
            // OR the framework is used without the need for serving files (such as views).
            logger.info(AssetRouter.class.getName() + " did not find any files in webapp - not serving any files then");
            return exclusions;
        }
        
        Set<String> resourcePaths = new TreeSet<>(collect);//servletContext.getResourcePaths("/");
        resourcePaths.removeIf( path -> path.contains("-INF") || path.contains("-inf"));
    
        // We still need to also remove the views folder from being processed by other handlers
        resourcePaths.removeIf( path -> path.contains(TemplateRendererEngine.TEMPLATES_FOLDER));
        
        // Add the remaining paths to exclusions
        for (String path : resourcePaths) {
            // add leading slash
            if (path.charAt(0) != '/')
                path = '/' + path;
            
            // remove the last slash
            if (path.charAt(path.length()-1) == '/')
                path = path.substring(0, path.length()-1);
            exclusions.add(path);
        }
        
        return exclusions;
    }
}
