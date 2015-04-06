package net.javapla.jawn.templatemanagers.stringtemplate;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import net.javapla.jawn.parsers.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationReader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String SITE_FILE = "site.json";

    private SiteConfiguration conf;

    public ConfigurationReader(String templateFolder, String controller) {
        Path rootFolder = Paths.get(templateFolder);

        // find eventual extra configurations in the controller folder
        SiteConfiguration localConf = readSiteFile(rootFolder.resolve(controller)); // we skip path.controller+'/'+path.method because we only look for other configurations on controller
        
        
        if (localConf.overrideDefault) {
            conf = localConf;
        } else {
            // find root site_file and eventual extra configurations of the controller folder
            conf = readSiteFile(rootFolder);
            mergeConfigurations(localConf);
        }
    }

    private SiteConfiguration readSiteFile(Path folder) {
        Path rootFile = folder.resolve(SITE_FILE);
        if ( !Files.exists(rootFile) ) return new SiteConfiguration();

        try (Reader r = new FileReader(rootFile.toFile())) {
            return JsonParser.parseObject(r, SiteConfiguration.class);
        } catch (IOException e) {
            log.error("Reading site_file", e);
        }
        return new SiteConfiguration();
    }

    /**
     * adding them uncritically
     * @param localConf
     */
    private void mergeConfigurations(SiteConfiguration localConf) {
        if (localConf.title != null && !localConf.title.isEmpty()) conf.title = localConf.title;
        conf.scripts.addAll(localConf.scripts);
        conf.styles.addAll(localConf.styles);
    }

    public List<String> readScripts() {
        return conf.scripts;
    }
    public List<String> readStyles() {
        return conf.styles;
    }
    

    public String title() {
        return conf.title;
    }
    
}

