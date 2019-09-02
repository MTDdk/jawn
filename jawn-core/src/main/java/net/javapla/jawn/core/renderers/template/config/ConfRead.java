package net.javapla.jawn.core.renderers.template.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.util.Modes;

/**
 * Empirical evidence tells us that caching the SiteConfiguration has a significant impact on performance in high load use.
 * This makes sense as ALL roundtrips to disk have great cost.
 * 
 * @author MTD
 */
@Singleton
//public class SiteConfigurationReader {
public class ConfRead {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public static final String SITE_FILE = "site.json"; //TODO move to jawn_defaults.properties
    /** needs to NOT start with a '/' as we are using this string to read from the filesystem */
    public static final String SCRIPT_STANDARD_FOLDER = "js/"; //TODO move to jawn_defaults.properties
    /** needs to NOT start with a '/' as we are using this string to read from the filesystem */
    public static final String STYLE_STANDARD_FOLDER = "css/"; //TODO move to jawn_defaults.properties
    
    private final ObjectMapper mapper;
    private final DeploymentInfo deploymentInfo;
    private final Modes mode;
    //private final HashMap<String, SiteConfiguration> configurationCache = new HashMap<>();
    // README: ConcurrentHashMap might deadlock with the same hash - so do we really need the concurrency?
    // https://stackoverflow.com/questions/43861945/deadlock-in-concurrenthashmap
    
    //private final HashMap<String, Site> cachedSiteObjs = new HashMap<>();
    
    @Inject
    public ConfRead(final ObjectMapper mapper, final DeploymentInfo deploymentInfo, final Modes mode) {
        this.mapper = mapper;
        this.deploymentInfo = deploymentInfo;
        this.mode = mode;
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
     */
    public Site load(View view) {
        readSiteFile(view.path());
        
        return null;
    }
    
    private SiteConfiguration readSiteFile(String folder) {
        
        String p = deploymentInfo.getRealPath(folder + '/' + SITE_FILE);
        try (Reader r = deploymentInfo.resourceAsReader(p)) {
            SiteConfiguration configuration = mapper.readValue(r, SiteConfiguration.class);
            
            decorateLocalResourceLinks(configuration.scripts, SCRIPT_STANDARD_FOLDER);
            decorateLocalResourceLinks(configuration.styles, STYLE_STANDARD_FOLDER);
            
            return configuration;
        } catch (IOException e) {
            log.error("Reading site_file {} \n{}", p, e.getMessage());
        }
        
        return new SiteConfiguration();
    }
    
    
    /**
     * Prefixes local resources with css/ or js/.
     * "Local" is defined by not starting with 'http.*' or 'ftp.*'
     * 
     * Adds a version query param to local resources.
     * The <code>version</code> is currently just an epoch
     */
    private final void decorateLocalResourceLinks(final SiteConfiguration.Tag[] links, final String prefix) {
        if (links == null) return;
        
        for(SiteConfiguration.Tag link : links) {
            if (isLocal(link.url)) {
                link.url = deploymentInfo.translateIntoContextPath( toAddOrNotToAddModified(link.url, prefix) );
            }
        }
    }
    
    final static boolean isLocal(String url) {
        //return !(url.matches("^(ht|f)tp.*") || url.startsWith("//"));
        
        //assert (url.length() > 2);
        return ! (
            url.startsWith("http", 0) ||
            url.startsWith("ftp", 0) ||
            url.startsWith("//", 0)
            ); 
    }
    
    final String toAddOrNotToAddModified(final String url, final String prefix) {
        StringBuilder result = new StringBuilder(1 + 4 + url.length() + 3 + 13);// "/"=1, "http|ftp|//"=4,..,"?v="=3,lastModified=13
        result.append(prefix); // length 3 or 4
        result.append(url);
        
        File resolved = new File(result.toString());
        System.out.println(resolved + "   ..  " + resolved.lastModified());
        
        // prepending with '/' makes the resource not depending on the URL it is called from
        result.insert(0, '/'); // length 1
        
        try (InputStream stream = deploymentInfo.resourceAsStream(result.toString())) {
            
        } catch (IOException e) {
        }
        
        
        
        if (resolved.exists() /*&& resolved.canRead()*/) { // TODO at this point we actually could do some minification as well
            result.append("?v="); // length 3
            result.append(resolved.lastModified()); // length 13
            return result.toString();
        } else {
            // README: this is frankly mostly for testing purposes - probably should be omitted all together
            // log.debug("File not found:: " + resolved + " - Perhaps a spelling error?");
            return result.toString();
        }
    }
}
