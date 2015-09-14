package net.javapla.jawn.core.templates.config;

public class Site {

    public final String url,
                        title,
                        language,
                        scripts,
                        styles;
    
    public final String content;
    
    public Site(String url, String title, String language, String scripts, String styles, String content) {
        this.url = url;
        this.title = title;
        this.language = language;
        this.scripts = scripts;
        this.styles = styles;
        this.content = content;
    }
}
