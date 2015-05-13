package net.javapla.jawn.core.templates.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.javapla.jawn.core.PropertiesImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * 
 * Empirical evidence tells us that caching the SiteConfiguration has a significant impact on performance in high load use.
 * This makes sense as all roundtrips to disk cost.
 * 
 * @author MTD
 */
public class ConfigurationReader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String SITE_FILE = "site.json";

    private final ObjectMapper mapper;
    private final Map<String, SiteConfiguration> configurationCache;
    private final boolean useCache;
    

    @Inject
    public ConfigurationReader(ObjectMapper mapper, PropertiesImpl properties) {
        this.mapper = mapper;
        this.configurationCache = new ConcurrentHashMap<>();
        this.useCache = properties.isProd();
    }
    
    public SiteConfiguration read(String templateFolder, String controller) {
        Path rootFolder = Paths.get(templateFolder);

        // find eventual extra configurations in the controller folder
        SiteConfiguration localConf = readSiteFileWithCache(rootFolder.resolve(controller)); // we skip path.controller+'/'+path.method because we only look for other configurations on controller
        
        if (localConf.overrideDefault) {
            return localConf;
        } else {
            // find root site_file and eventual extra configurations of the controller folder
            SiteConfiguration conf = readSiteFileWithCache(rootFolder);
            mergeConfigurations(conf, localConf);
            return conf;
        }
    }
    
    private SiteConfiguration readSiteFileWithCache(Path folder) {
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
     * adding them uncritically
     * @param localConf
     */
    private void mergeConfigurations(SiteConfiguration globalConf, SiteConfiguration localConf) {
        if (localConf.title != null && !localConf.title.isEmpty()) globalConf.title = localConf.title;
        globalConf.scripts.addAll(localConf.scripts);
        globalConf.styles.addAll(localConf.styles);
    }
}
