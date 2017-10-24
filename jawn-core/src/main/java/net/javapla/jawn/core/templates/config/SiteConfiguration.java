package net.javapla.jawn.core.templates.config;

import java.util.Arrays;

public class SiteConfiguration implements Cloneable {

    public String title;
    public Script[] scripts;
    public Style[] styles;
    
    public boolean overrideDefault;
    
    public SiteConfiguration() {
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
    
    public static class Style {
    	public String url;
    	public String integrity;
    	public String crossOrigin;
    	
    	public Style() {}
    	public Style(String url, String integrity, String crossOrigin) {
    		this.url = url;
    		this.integrity = integrity;
    		this.crossOrigin = crossOrigin;
    	}
    	public Style(String url) {
    		this(url, null, null);
    	}
    	
    }
    
    public static class Script {
    	public String url;
    	public String integrity;
    	public String crossOrigin;
    	public boolean async;
    	public boolean defer;
    	
    	public Script() {}
    	public Script(String url, boolean all) { this(url, all, all); }
    	public Script(String url, boolean async, boolean defer) {
    		this.url = url;
    		this.async = async;
    		this.defer = defer;
    	}
    	
    	public String getUrl() { return url; }
    	
    	public Script url(String url) {
    	    return new Script(url, async, defer);
    	}
    }    
}
