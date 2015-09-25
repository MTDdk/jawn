package net.javapla.jawn.core.templates.config;

import net.javapla.jawn.core.util.Modes;

public class Site {

    public final String url,
                        title,
                        language,
                        scripts,
                        styles;
    
    public final String content;
    
    public final Modes mode;
    
    public Site(String url, String title, String language, String scripts, String styles, String content, Modes mode) {
        this.url = url;
        this.title = title;
        this.language = language;
        this.scripts = scripts;
        this.styles = styles;
        this.content = content;
        this.mode = mode;
    }
    
    public boolean isDev() { return mode == Modes.DEV; }
    public boolean isProd() { return mode == Modes.PROD; }
    public boolean isTest() { return mode == Modes.TEST; }
}
