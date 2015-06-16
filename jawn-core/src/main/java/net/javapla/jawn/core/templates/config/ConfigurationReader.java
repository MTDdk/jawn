package net.javapla.jawn.core.templates.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * 
 * Empirical evidence tells us that caching the SiteConfiguration has a significant impact on performance in high load use.
 * This makes sense as all roundtrips to disk have great cost.
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
            // find root site_file and eventual extra configurations of the controller folder
            SiteConfiguration conf = readSiteFileWithCache(rootFolder, useCache);
            mergeConfigurations(conf, localConf);
            return conf;
        }
    }
    
    private SiteConfiguration readSiteFileWithCache(Path folder, boolean useCache) {
        if (useCache) {
            return configurationCache.computeIfAbsent(folder.toString(), f -> readSiteFile(folder));
        } else {
            return readSiteFile(folder);
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
     * @param globalConf
     *      The default configuration
     * @param localConf
     *      The controller configuration
     */
    private void mergeConfigurations(SiteConfiguration globalConf, SiteConfiguration localConf) {
        if (localConf.title != null && !localConf.title.isEmpty()) globalConf.title = localConf.title;
        globalConf.scripts.addAll(localConf.scripts);
        globalConf.styles.addAll(localConf.styles);
    }
}
