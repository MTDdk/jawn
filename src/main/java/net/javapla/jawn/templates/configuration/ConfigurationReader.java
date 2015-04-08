package net.javapla.jawn.templates.configuration;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

public class ConfigurationReader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String SITE_FILE = "site.json";

    private final ObjectMapper mapper;
    

    @Inject
    public ConfigurationReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    public SiteConfiguration read(String templateFolder, String controller) {
        Path rootFolder = Paths.get(templateFolder);

        // find eventual extra configurations in the controller folder
        SiteConfiguration localConf = readSiteFile(rootFolder.resolve(controller)); // we skip path.controller+'/'+path.method because we only look for other configurations on controller
        
        if (localConf.overrideDefault) {
            return localConf;
        } else {
            // find root site_file and eventual extra configurations of the controller folder
            SiteConfiguration conf = readSiteFile(rootFolder);
            mergeConfigurations(conf, localConf);
            return conf;
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

