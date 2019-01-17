package net.javapla.jawn.core.templates.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.http.Context;

/**
 * 
 * Empirical evidence tells us that caching the SiteConfiguration has a significant impact on performance in high load use.
 * This makes sense as ALL roundtrips to disk have great cost.
 * 
 * @author MTD
 */
public class SiteConfigurationReader {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public static final String SITE_FILE = "site.json";
    /** needs to NOT start with a '/' as we are using this string to read from the filesystem */
    public static final String SCRIPT_STANDARD_FOLDER = "js/";
    /** needs to NOT start with a '/' as we are using this string to read from the filesystem */
    public static final String STYLE_STANDARD_FOLDER = "css/";
    
    private final ObjectMapper mapper;
    private final DeploymentInfo deploymentInfo;
    private final HashMap<String, SiteConfiguration> configurationCache = new HashMap<>();
    // README: ConcurrentHashMap might deadlock with the same hash - so do we really need the concurrency?
    // https://stackoverflow.com/questions/43861945/deadlock-in-concurrenthashmap
    
    private final HashMap<String, Site> cachedSiteObjs = new HashMap<>();
    
    @Inject
    public SiteConfigurationReader(ObjectMapper mapper, DeploymentInfo deploymentInfo) {
        this.mapper = mapper;
        this.deploymentInfo = deploymentInfo;
    }
    
    /**
     * @see #read(String, String, String, boolean)
     */
    public SiteConfiguration read(String templateFolder, String controller, String layoutPath) {
        return read(templateFolder, controller, layoutPath, false);
    }
    /**
     * Reads the {@value #SITE_FILE} of the <code>templateFolder</code> and merges with <code>templateFolder/controller</code>,
     * if any exists.
     * 
     * flow:
     * <ol>
     * <li>look in <code>templateFolder/controller</code></li>
     * <li>look in <code>templateFolder/layoutPath</code> and merge (if the layoutPath is not already in <code>templateFolder/controller</code>)</li>
     * <li>continue on to look in <code>templateFolder</code>(root) and merge</li>
     * </ol>
     * 
     * @param templateFolder 
     *      The root folder to read from. The {@value #SITE_FILE} in this location is the default configuration file.
     *      Same as the index.html is the default layout.
     * @param controller
     *      The folder for the controller that is executed. This folder might hold an additional {@value #SITE_FILE},
     *      which will be used to merge with the default configuration file.
     *      This takes precedence over the values in default configuration file.
     * @param layoutPath
     *      A layout can be specified which is not at <code>templateFolder</code>.<br>
     *      Might be an empty string if default layout is used.<br>
     *      Can be the same as controller if both are "index"
     * @param useCache
     *      Use caching of the read configuration
     * @return
     *      The read configuration.
     *      This might be the default configuration, or the merged configuration, or only the controller configuration
     *      if the controller configuration has {@link SiteConfiguration#overrideDefault} set to override everything.
     */
    public SiteConfiguration read(String templateFolder, String controller, String layoutPath, boolean useCache) {
        Path rootFolder = Paths.get(templateFolder);

        // find eventual extra configurations in the controller folder
        // we skip path.controller+'/'+path.method because we only look for other configurations on controller level
        SiteConfiguration controllerConf = readSiteFileWithCache(rootFolder.resolve(controller), useCache);
        if (controllerConf.overrideDefault)
            return controllerConf;
            
        // Use the #SITE_FILE near the index.html (a.k.a. layout), if layout is NOT located within the controller path
        // In this way we have the conf from controller AND near the layout 
        // (which might need some information from the conf near it)
        if (!layoutPath.isEmpty() && !isLayoutInControllerFolder(controller, layoutPath)) {
            Path layoutFolder = rootFolder.resolve(layoutPath);
            SiteConfiguration controllerPlusLayoutConf = mergeSiteFilesWithCache(layoutFolder, controller, controllerConf, useCache);
            if (controllerPlusLayoutConf.overrideDefault) {
                return controllerPlusLayoutConf;
            }
            
            controllerConf = controllerPlusLayoutConf;
            controller = createPathIdentification(layoutPath, controller);
        }
            
        // Aaand we also look for it at the very root
        return mergeSiteFilesWithCache(rootFolder, controller, controllerConf, useCache);
    }
    
    public Site retrieveSite(Context ctx, SiteConfiguration conf, String path, String content, boolean useCache) {
        if (useCache) {
            // essentially caching a partial Site object (because a fresh 'content' needs to be injected always) 
            return cachedSiteObjs.computeIfAbsent(path, key -> createCachableSite(ctx, conf)).content(content);
        } else {
            return createCachableSite(ctx, conf).content(content);
        }
    }
    
    private Site createCachableSite(Context ctx, SiteConfiguration conf) {
        // does not contain 'content'
        return Site.builder(ctx.mode()) // state the current mode
                    .url(ctx.path()) // add the URL
                    .title(conf.title) // add title
                    
                    //add scripts
                    .scripts(conf.scripts)
                    .styles(conf.styles)
                    .build();
    }
    
    private final static boolean isLayoutInControllerFolder(final String controller, final String layoutPath) {
        return  layoutPath.length() > 1  // layout is more than just '/'
            && (layoutPath.length() == controller.length() || layoutPath.length() == controller.length()+1) 
            &&  layoutPath.startsWith(controller);
    }
    
    private SiteConfiguration readSiteFileWithCache(Path folder, boolean useCache) {
        if (useCache) {
            return configurationCache.computeIfAbsent(folder.toString(), f -> readSiteFile(folder));
        } else {
            return readSiteFile(folder);
        }
    }
    
    private SiteConfiguration mergeSiteFilesWithCache(Path rootFolder, String controller, SiteConfiguration controllerConf, boolean useCache) {
        if (useCache) {
            String rootPlusController = createPathIdentification(rootFolder.toString(), controller);
            SiteConfiguration mergedConfiguration = configurationCache.get(rootPlusController);
            if (mergedConfiguration == null) {
                mergedConfiguration = mergeConfigurations(readSiteFileWithCache(rootFolder, true), controllerConf);
                configurationCache.put(rootPlusController, mergedConfiguration);
            }
            return mergedConfiguration;
        } else {
            // find root site_file and eventual extra configurations of the controller folder
            SiteConfiguration rootConf = readSiteFileWithCache(rootFolder, useCache);
            return mergeConfigurations(rootConf, controllerConf);
        }
    }
    
    private String createPathIdentification(String folder, String controller) {
        return new StringBuilder().append(folder).append('+').append(controller).toString();
    }

    private final SiteConfiguration readSiteFile(Path folder) {
        Path rootFile = folder.resolve(SITE_FILE);
        if ( !Files.exists(rootFile) ) return new SiteConfiguration();

        try (Reader r = new FileReader(rootFile.toFile())) {
            SiteConfiguration configuration = mapper.readValue(r, SiteConfiguration.class);
            
            Path parent = folder.getParent(); // we always assume that "js/" and "css/" is at the same level as "folder" (which is most likely "webapp/views"
            decorateLocalResourceLinks(configuration.scripts, SCRIPT_STANDARD_FOLDER, parent);
            decorateLocalResourceLinks(configuration.styles, STYLE_STANDARD_FOLDER, parent);
            
            return configuration;
        } catch (IOException e) {
            log.error("Reading site_file {} \n{}", rootFile, e.getMessage());
        }
        return new SiteConfiguration();
    }

    /**
     * Letting <code>controllerConf</code> take precedence over <code>rootConf</code>.
     * <br>Adding them uncritically
     * 
     * <p>Does not do any overwrites on any of the parameters.
     * Instead it creates a new object with all the merges
     * 
     * @param rootConf
     *      The default configuration at the root of the templateFolder
     * @param controllerConf
     *      The controller configuration - might be empty, if no {@value #SITE_FILE} is found within the controller path
     * @return The merged SiteConfiguration
     */
    private SiteConfiguration mergeConfigurations(SiteConfiguration rootConf, SiteConfiguration controllerConf) {
        // do not do any overwrites to the rootConf
        // clone it instead (or create an entirely new object)
        SiteConfiguration topConf = rootConf.clone();
        if (controllerConf.title != null && !controllerConf.title.isEmpty()) topConf.title = controllerConf.title;
        
        if (controllerConf.scripts != null) {
            if (topConf.scripts != null) {
                SiteConfiguration.Tag[] scripts = new SiteConfiguration.Tag[topConf.scripts.length + controllerConf.scripts.length];
                System.arraycopy(topConf.scripts, 0, scripts, 0, topConf.scripts.length);
                System.arraycopy(controllerConf.scripts, 0, scripts, topConf.scripts.length, controllerConf.scripts.length);
                topConf.scripts = scripts;
            } else {
                topConf.scripts = Arrays.copyOf(controllerConf.scripts, controllerConf.scripts.length);
            }
        }
        
        if (controllerConf.styles != null) {
            if (topConf.styles != null) {
            	SiteConfiguration.Tag[] styles = new SiteConfiguration.Tag[topConf.styles.length + controllerConf.styles.length];
                System.arraycopy(topConf.styles, 0, styles, 0, topConf.styles.length);
                System.arraycopy(controllerConf.styles, 0, styles, topConf.styles.length, controllerConf.styles.length);
                topConf.styles = styles;
            } else {
                topConf.styles = Arrays.copyOf(controllerConf.styles, controllerConf.styles.length);
            }
        }
        
        return topConf;
    }
    
    
    /**
     * Prefixes local resources with css/ or js/.
     * "Local" is defined by not starting with 'http.*' or 'ftp.*'
     * 
     * Adds a version query param to local resources.
     * The <code>version</code> is currently just an epoch
     */
    private final void decorateLocalResourceLinks(final SiteConfiguration.Tag[] links, final String prefix, final Path root) {
        if (links == null) return;
        
        for(SiteConfiguration.Tag link : links) {
            if (isLocal(link.url)) {
                link.url = deploymentInfo.translateIntoContextPath( toAddOrNotToAddModified(link.url, prefix, root) );
            }
        }
    }
    
    final static boolean isLocal(String url) {
        //return !(url.matches("^(ht|f)tp.*") || url.startsWith("//"));
        
        //assert (url.length() < 5);
        return ! (
            url.startsWith("http", 0) ||
            url.startsWith("ftp", 0) ||
            url.startsWith("//", 0)
            ); 
    }
    
    final String toAddOrNotToAddModified(final String url, final String prefix, final Path root) {
        StringBuilder result = new StringBuilder(1 + 4 + url.length() + 3 + 13);// '/'=1, 'http|ftp|//'=4,..,'?v='=3,lastModified=13
        result.append(prefix); // length 3 or 4
        result.append(url);
        
        File resolved = root.resolve(result.toString()).toFile();
        
        // prepending with '/' makes the resource not depending on the URL it is called from
        result.insert(0, '/'); // length 1
        
        if (resolved.exists() /*&& resolved.canRead()*/) { // TODO at this point we actually could do some minification as well
            result.append("?v="); // length 3
            result.append(resolved.lastModified()); // length 13
            return result.toString();
        } else {
            // README: this is frankly mostly for testing purposes - probably should be omitted all together
            log.error("File not found:: " + result + " - Perhaps a spelling error?");
            return result.toString();
        }
    }
}
