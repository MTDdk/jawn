package net.javapla.jawn.core.templates.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * 
 * Empirical evidence tells us that caching the SiteConfiguration has a significant impact on performance in high load use.
 * This makes sense as ALL roundtrips to disk have great cost.
 * 
 * @author MTD
 */
public class ConfigurationReader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String SITE_FILE = "site.json";

    private final ObjectMapper mapper;
    private final Map<String, SiteConfiguration> configurationCache;
    

    @Inject
    public ConfigurationReader(ObjectMapper mapper) {
        this.mapper = mapper;
        this.configurationCache = new ConcurrentHashMap<>();
    }
    
    /**
     * @see #read(String, String, boolean)
     */
    public SiteConfiguration read(String templateFolder, String controller) {
        return read(templateFolder, controller, false);
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
     * @param useCache
     *      Use caching of the read configuration
     * @return
     *      The read configuration.
     *      This might be the default configuration, or the merged configuration, or only the controller configuration
     *      if the controller configuration has {@link SiteConfiguration#overrideDefault} set to override everything.
     */
    public SiteConfiguration read(String templateFolder, String controller, boolean useCache) {
        Path rootFolder = Paths.get(templateFolder);

        // find eventual extra configurations in the controller folder
        // we skip path.controller+'/'+path.method because we only look for other configurations on controller
        SiteConfiguration localConf = readSiteFileWithCache(rootFolder.resolve(controller), useCache);
        
        if (localConf.overrideDefault) {
            return localConf;
        } else {
            /*// find root site_file and eventual extra configurations of the controller folder
            SiteConfiguration conf = readSiteFileWithCache(rootFolder, useCache);
            
            SiteConfiguration merged = mergeConfigurations(conf, localConf);
            return merged;*/
            return mergeSiteFilesWithCache(rootFolder, controller, localConf, useCache);
        }
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
            String rootPlusController = MessageFormat.format("{0}+{1}", rootFolder.toString(), controller);
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

    private SiteConfiguration readSiteFile(Path folder) {
        Path rootFile = folder.resolve(SITE_FILE);
        if ( !Files.exists(rootFile) ) return new SiteConfiguration();

        try (Reader r = new FileReader(rootFile.toFile())) {
            return mapper.readValue(r, SiteConfiguration.class);
        } catch (IOException e) {
            log.error("Reading site_file", e);
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
        topConf.scripts.addAll(localConf.scripts);
        topConf.styles.addAll(localConf.styles);
        
        return topConf;
    }
}
