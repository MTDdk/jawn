package net.javapla.jawn.core.templates.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SiteConfiguration implements Cloneable {

    public String title;
    public Tag[] scripts;
    public Tag[] styles;
    
    public boolean overrideDefault;
    
    public SiteConfiguration() { }
    
    @Override
    public String toString() {
        return String.format("SiteConfiguration: %s %s %s", title, Arrays.toString(scripts), Arrays.toString(styles));
    }
    
    @Override
    protected SiteConfiguration clone() {
        SiteConfiguration conf = new SiteConfiguration();
        conf.title = this.title;
        conf.overrideDefault = this.overrideDefault;
        
        if (scripts != null)
            conf.scripts = Arrays.stream(this.scripts).map(tag -> tag.clone()).toArray(SiteConfiguration.Tag[]::new);
        if (styles != null)
            conf.styles = Arrays.stream(this.styles).map(tag -> tag.clone()).toArray(SiteConfiguration.Tag[]::new);
        
        return conf;
    }
    
    public static class Tag {
        public String url;
        public Map<String, String> attr = Collections.emptyMap();
        
        public Tag() {} // needed by Jackson serialisation
        public Tag(String url) {
            this.url = url;
        }
        public Tag(String url, Map<String, String> attr) {
            this.url = url;
            this.attr = attr;
        }
        
        @Override
        public String toString() {
            return String.format("Tag %s %s", url,  attr.toString());
        }
        
        @Override
        protected Tag clone() {
            return new Tag(url, new HashMap<>(attr));
        }
    }
    
}
