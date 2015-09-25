package net.javapla.jawn.core.templates.config;

import java.util.ArrayList;
import java.util.List;

public class SiteConfiguration implements Cloneable {

    public String title;
    public List<String> scripts;
    public List<String> styles;
    
    public boolean overrideDefault;
    
    public SiteConfiguration() {
        this.scripts = new ArrayList<String>();
        this.styles = new ArrayList<String>();
    }
    
    @Override
    public String toString() {
        return String.format("SiteConfiguration: %s %s %s", title, scripts, styles);
    }
    
    @Override
    protected SiteConfiguration clone() {
        SiteConfiguration conf = new SiteConfiguration();
        conf.title = this.title;
        conf.scripts.addAll(this.scripts);
        conf.styles.addAll(this.styles);
        conf.overrideDefault = this.overrideDefault;
        return conf;
    }
}
