package net.javapla.jawn.core.templates.config;

import java.util.Arrays;

public class SiteConfiguration implements Cloneable {

    public String title;
    public Script[]/*List<String>*/ scripts;
    public String[]/*List<String>*/ styles;
    
    public boolean overrideDefault;
    
    public SiteConfiguration() {
//        this.scripts = new ArrayList<String>();
//        this.styles = new ArrayList<String>();
    }
    
    @Override
    public String toString() {
        return String.format("SiteConfiguration: %s %s %s", title, Arrays.toString(scripts), Arrays.toString(styles));
    }
    
    @Override
    protected SiteConfiguration clone() {
        SiteConfiguration conf = new SiteConfiguration();
        conf.title = this.title;
        //conf.scripts.addAll(this.scripts);
        if (scripts != null)
        conf.scripts = Arrays.copyOf(this.scripts, scripts.length);
//        conf.styles.addAll(this.styles);
        if (styles != null)
        conf.styles = Arrays.copyOf(this.styles, styles.length);
        conf.overrideDefault = this.overrideDefault;
        return conf;
    }
    
    public static class Script {
    	public String url;
    	public boolean async;
    	
    	public Script(){}
    	public Script(String url, boolean async) {
    		this.url = url;
    		this.async = async;
    	}
    	
    	public String getUrl() { return url; }
    }
}
