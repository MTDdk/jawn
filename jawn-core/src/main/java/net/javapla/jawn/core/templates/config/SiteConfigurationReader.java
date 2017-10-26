package net.javapla.jawn.core.templates.config;

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
    public static final String SCRIPT_STANDARD_FOLDER = "/js/";
    public static final String STYLE_STANDARD_FOLDER = "/css/";

    private final ObjectMapper mapper;
    private final DeploymentInfo deploymentInfo;
    private final /*Concurrent*/HashMap<String, SiteConfiguration> configurationCache;
    // README: ConcurrentHashMap might deadlock with the same hash - so do we really need the concurrency?
    // https://stackoverflow.com/questions/43861945/deadlock-in-concurrenthashmap
    
    @Inject
    public SiteConfigurationReader(ObjectMapper mapper, DeploymentInfo deploymentInfo) {
        this.mapper = mapper;
        this.deploymentInfo = deploymentInfo;
        this.configurationCache = new /*Concurrent*/HashMap<>();
    }
    
    /**
     * @see #read(String, String, String, boolean)
     */
    public SiteConfiguration read(String templateFolder, String controller, String layout) {
        return read(templateFolder, controller, layout, false);
    }
    /**
     * Reads the {@value #SITE_FILE} of the <code>templateFolder</code> and merges with <code>templateFolder/controller</code>,
     * if any exists.
     * 
     * @param templateFolder 
     *      The root folder to read from. The {@value #SITE_FILE} in this location is the default configuration file.
     *      Same as the index.html.st is the default layout.
     * @param controller
     *      The folder for the controller that is executed. This folder might hold an additional {@value #SITE_FILE},
     *      which will be used to merge with the default configuration file.
     *      This takes precedence over the values in default configuration file.
     * @param layout
     * 
     * @param useCache
     *      Use caching of the read configuration
     * @return
     *      The read configuration.
     *      This might be the default configuration, or the merged configuration, or only the controller configuration
     *      if the controller configuration has {@link SiteConfiguration#overrideDefault} set to override everything.
     */
    public SiteConfiguration read(String templateFolder, String controller, String layout, boolean useCache) {
        Path rootFolder = Paths.get(templateFolder);

        // find eventual extra configurations in the controller folder
        // we skip path.controller+'/'+path.method because we only look for other configurations on controller level
        SiteConfiguration localConf = readSiteFileWithCache(rootFolder.resolve(controller), useCache);
        if (localConf.overrideDefault)
            return localConf;
            
        // Use the SITE_FILE near the index.html
        if (layout.length() > 1) {
            Path layoutFolder = rootFolder.resolve(layout);
            SiteConfiguration mergedConf = mergeSiteFilesWithCache(layoutFolder, controller, localConf, useCache);
            if (mergedConf.overrideDefault) {
                return mergedConf;
            }
            
            localConf = mergedConf;
            controller = createPathIdentification(layout, controller);
        }
        
        //SiteConfiguration mergedConf = mergeSiteFilesWithCache(layoutFolder, controller, localConf, useCache);
            
        //} else {
            /*// find root site_file and eventual extra configurations of the controller folder
            SiteConfiguration conf = readSiteFileWithCache(rootFolder, useCache);
            
            SiteConfiguration merged = mergeConfigurations(conf, localConf);
            return merged;*/
            
            
            // Or we simply just try to look for it at the very root
            return mergeSiteFilesWithCache(rootFolder, controller, localConf, useCache);
        
    }
    
    private SiteConfiguration readSiteFileWithCache(Path folder, boolean useCache) {
        if (useCache) {
            return configurationCache.computeIfAbsent(folder.toString(), f -> readSiteFile(folder));
        } else {
            return readSiteFile(folder);
        }
    }
    
    private SiteConfiguration mergeSiteFilesWithCache(Path rootFolder, String controller, SiteConfiguration localConf, boolean useCache) {
        if (useCache) {
            String rootPlusController = createPathIdentification(rootFolder.toString(), controller);
            return configurationCache
                        .computeIfAbsent(rootPlusController, 
                            f -> mergeConfigurations(readSiteFileWithCache(rootFolder, true), localConf)
                        );
        } else {
            // find root site_file and eventual extra configurations of the controller folder
            SiteConfiguration conf = readSiteFileWithCache(rootFolder, useCache);
            return mergeConfigurations(conf, localConf);
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
            prefixResourceLinks(configuration.scripts, SCRIPT_STANDARD_FOLDER);
            prefixResourceLinks(configuration.styles, STYLE_STANDARD_FOLDER);
            return configuration;
        } catch (IOException e) {
            log.error("Reading site_file {} \n{}", rootFile, e.getMessage());
        }
        return new SiteConfiguration();
    }

    /**
     * Letting localConf take precedence over globalConf.
     * Adding them uncritically
     * 
     * <p>Does not do any overwrites on any of the parameters.
     * Instead it creates a new object with all the merges
     * 
     * @param globalConf
     *      The default configuration
     * @param localConf
     *      The controller configuration
     * @return The merged SiteConfiguration
     */
    private SiteConfiguration mergeConfigurations(SiteConfiguration globalConf, SiteConfiguration localConf) {
        // do not do any overwrites to the globalConf
        // clone it instead (or create an entirely new object instead)
        SiteConfiguration topConf = globalConf.clone();
        if (localConf.title != null && !localConf.title.isEmpty()) topConf.title = localConf.title;
        //topConf.scripts.addAll(localConf.scripts);
        //topConf.styles.addAll(localConf.styles);
        
        if (localConf.scripts != null) {
            if (topConf.scripts != null) {
                SiteConfiguration.Script[] scripts = new SiteConfiguration.Script[topConf.scripts.length + localConf.scripts.length];
                System.arraycopy(topConf.scripts, 0, scripts, 0, topConf.scripts.length);
                System.arraycopy(localConf.scripts, 0, scripts, topConf.scripts.length, localConf.scripts.length);
                topConf.scripts = scripts;
            } else {
                topConf.scripts = Arrays.copyOf(localConf.scripts, localConf.scripts.length);
            }
        }
        
        if (localConf.styles != null) {
            if (topConf.styles != null) {
            	SiteConfiguration.Style[] styles = new SiteConfiguration.Style[topConf.styles.length + localConf.styles.length];
                System.arraycopy(topConf.styles, 0, styles, 0, topConf.styles.length);
                System.arraycopy(localConf.styles, 0, styles, topConf.styles.length, localConf.styles.length);
                topConf.styles = styles;
            } else {
                topConf.styles = Arrays.copyOf(localConf.styles, localConf.styles.length);
            }
        }
        
        return topConf;
    }
    
    
    /**
     * Prefixes local resources with css/ or js/.
     * "Local" is defined by not starting with 'http.*' or 'ftp.*'
     */
    private final void prefixResourceLinks(final SiteConfiguration.Link[] links, final String prefix) {
    	if(links != null) {
    		for(SiteConfiguration.Link link : links) {
                if (!(link.url.matches("^(ht|f)tp.*") || link.url.startsWith("//")))
                    link.url = /*deploymentInfo.translateIntoContextPath(*/prefix + link.url/*)*/;
    		}
    	}
    }
    
}
