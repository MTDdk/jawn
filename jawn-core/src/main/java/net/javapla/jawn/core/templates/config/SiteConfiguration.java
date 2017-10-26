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
    
    public static class Link {
    	public String url;
    }
    
    public static class Style extends Link {
    	public String integrity;
    	public String crossorigin;
    	
    	public Style() {}
    	public Style(String url) {
    		this(url, null, null);
    	}
    	public Style(String url, String integrity, String crossorigin) {
    		this.url = url;
    		this.integrity = integrity;
    		this.crossorigin = crossorigin;
    	}
    	
    }
    
    public static class Script extends Link {
    	public String type;
    	public boolean async;
    	public boolean defer;
    	public String integrity;
    	public String crossorigin;
    	
    	public Script() {}
    	public Script(String url, boolean async, boolean defer) {
    		this(url, null, null, null, async, defer);
    	}
    	public Script(String url, String type, String integrity, String crossorigin, boolean async, boolean defer) {
    		this.url = url;
    		this.type = type;
    		this.integrity = integrity;
    		this.crossorigin = crossorigin;
    		this.async = async;
    		this.defer = defer;
    	}
    	
    }    
}
