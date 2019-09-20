package net.javapla.jawn.core.renderers.template.config;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.util.StringUtil;

class SiteConfigurationReader {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public static final String SITE_FILE = "site.json"; //TODO move to jawn_defaults.properties
    /** needs to NOT start with a '/' as we are using this string to read from the filesystem */
    public static final String SCRIPT_STANDARD_FOLDER = "js/"; //TODO move to jawn_defaults.properties
    /** needs to NOT start with a '/' as we are using this string to read from the filesystem */
    public static final String STYLE_STANDARD_FOLDER = "css/"; //TODO move to jawn_defaults.properties
    
    private final ObjectMapper mapper;
    private final DeploymentInfo deploymentInfo;

    public SiteConfigurationReader(final ObjectMapper mapper, final DeploymentInfo deploymentInfo) {
        this.mapper = mapper;
        this.deploymentInfo = deploymentInfo;
    }
    
    /**
     * Reads the {@value #SITE_FILE} of the <code>root</code> and merges with <code>root/folder</code>,
     * if any exists.
     * 
     * flow:
     * <ol>
     * <li>look in <code>root/folder</code></li>
     * <li>(no longer in use) merge with <code>layout path</code></li>
     * <li>continue on to look in <code>root</code> and merge</li>
     * </ol>
     * @return 
     */
    public SiteConfiguration find(String folder) {
        // See if there is a configuration on the same level as the template
        SiteConfiguration controllerConfiguration = readSiteFile(folder);
        
        if (controllerConfiguration.overrideDefault || folder.equals("") || folder.equals("/"))
            return controllerConfiguration;
        
        SiteConfiguration root = readSiteFile("");
        
        return merge(root, controllerConfiguration);
    }
    
    /**
     * <code>first</code> is the base, and <code>second</code> will override the title, if it has any
     * 
     * All Tags will be merged.
     * 
     * {@link SiteConfiguration#overrideDefault} is not considered in the merge, as it is
     * expected to be false for both <code>first</code> and <code>second</code>
     * 
     * @param first
     * @param second
     * @return A new configuration with merged properties
     */
    public SiteConfiguration merge(SiteConfiguration first, SiteConfiguration second) {
        // Should NOT do any overrides of any of the SiteConfigurations, as this might alter cached versions.
        // Instead make sure to clone or recreate new objects
        
        SiteConfiguration resultingConf = first.clone();
        
        if (second.title != null && !second.title.isEmpty()) resultingConf.title = second.title;
        
        if (second.scripts != null) {
            if (resultingConf.scripts != null) {
                SiteConfiguration.Tag[] scripts = new SiteConfiguration.Tag[resultingConf.scripts.length + second.scripts.length];
                System.arraycopy(resultingConf.scripts, 0, scripts, 0, resultingConf.scripts.length);
                System.arraycopy(second.scripts, 0, scripts, resultingConf.scripts.length, second.scripts.length);
                resultingConf.scripts = scripts;
            } else {
                resultingConf.scripts = Arrays.copyOf(second.scripts, second.scripts.length);
            }
        }
        
        if (second.styles != null) {
            if (resultingConf.styles != null) {
                SiteConfiguration.Tag[] styles = new SiteConfiguration.Tag[resultingConf.styles.length + second.styles.length];
                System.arraycopy(resultingConf.styles, 0, styles, 0, resultingConf.styles.length);
                System.arraycopy(second.styles, 0, styles, resultingConf.styles.length, second.styles.length);
                resultingConf.styles = styles;
            } else {
                resultingConf.styles = Arrays.copyOf(second.styles, second.styles.length);
            }
        }
        
        return resultingConf;
    }
    
    public SiteConfiguration readSiteFile(String folder) {
        try (Reader r = deploymentInfo.viewResourceAsReader(StringUtil.blank(folder) ? SITE_FILE : folder + '/' + SITE_FILE)) {
            SiteConfiguration configuration = mapper.readValue(r, SiteConfiguration.class);
            
            decorateLocalResourceLinks(configuration.scripts, SCRIPT_STANDARD_FOLDER);
            decorateLocalResourceLinks(configuration.styles, STYLE_STANDARD_FOLDER);
            
            return configuration;
        } catch (IOException e) {
            log.error("Reading site_file {} \n{}", folder + '/' + SITE_FILE, e.getMessage());
            return new SiteConfiguration();
        }
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
        
        // prepending with '/' makes the resource not depending on the URL it is called from
        result.insert(0, '/'); // length 1
        
        long lastModified = deploymentInfo.resourceLastModified(result.toString());
        if (lastModified > -1) {//resolved.exists() /*&& resolved.canRead()*/) { // TODO at this point we actually could do some minification as well
            result.append("?v="); // length 3
            result.append(lastModified); // length 13
            return result.toString();
        } else {
            // log.debug("File not found:: " + resolved + " - Perhaps a spelling error?"); // README: this is frankly mostly for testing purposes - probably should be omitted all together
            return result.toString();
        }
    }
}
