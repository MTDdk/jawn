package net.javapla.jawn.core.templates.config;

public class Site {

    public final String title;
    public final String language;
    public final String scripts;
    public final String styles;
    
    public final String content;
    
    public Site(String title, String language, String scripts, String styles, String content) {
        this.title = title;
        this.language = language;
        this.scripts = scripts;
        this.styles = styles;
        this.content = content;
    }
}
