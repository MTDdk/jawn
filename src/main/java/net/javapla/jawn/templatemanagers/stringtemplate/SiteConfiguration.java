package net.javapla.jawn.templatemanagers.stringtemplate;

import java.util.ArrayList;
import java.util.List;

class SiteConfiguration {

    public String title;
    public List<String> scripts;
    public List<String> styles;
    
    public boolean overrideDefault;
    
    public SiteConfiguration() {
        this.scripts = new ArrayList<String>();
        this.styles = new ArrayList<String>();
    }
}
