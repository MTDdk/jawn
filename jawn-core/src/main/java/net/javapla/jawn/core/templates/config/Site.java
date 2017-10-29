package net.javapla.jawn.core.templates.config;

import net.javapla.jawn.core.util.Modes;

public class Site {

    public final String url,
                        title,
                        scripts,
                        styles;
    
    public final String content;
    
    public final Modes mode;
    
    protected Site(final String url, final String title, final String scripts, final String styles, final String content, final Modes mode) {
        this.url = url;
        this.title = title;
        this.scripts = scripts;
        this.styles = styles;
        this.content = content;
        this.mode = mode;
    }
    
    public boolean isDev() { return mode == Modes.DEV; }
    public boolean isProd() { return mode == Modes.PROD; }
    public boolean isTest() { return mode == Modes.TEST; }
    
    public static Site.Builder builder() {
        return new Site.Builder();
    }
    
    public static class Builder {
        public String 
            url,
            title,
            language,
            scripts,
            styles;

        public String content;

        public Modes mode;
        private Builder() {}
        
        public Site.Builder url(String url) {
            this.url = url;
            return this;
        }
        public Site.Builder title(String title) {
            this.title = title;
            return this;
        }
        public Site.Builder language(String language) {
            this.language = language;
            return this;
        }
        public Site.Builder scripts(String scripts) {
            this.scripts = scripts;
            return this;
        }
        public Site.Builder scripts(SiteConfiguration.Script[] links) {
            this.scripts = createLinks(links);
            return this;
        }
        public Site.Builder styles(String styles) {
            this.styles = styles;
            return this;
        }
        public Site.Builder styles(SiteConfiguration.Style[] links) {
            this.styles = createLinks(links);
            return this;
        }
        public Site.Builder content(String content) {
            this.content = content;
            return this;
        }
        public Site.Builder mode(Modes mode) {
            this.mode = mode;
            return this;
        }
        
        public Site build() {
            return new Site(url, title/*, language*/,scripts, styles,content,mode);
        }
        
        protected final String createLinks(SiteConfiguration.Script[] links) {
            final StringBuilder sb = new StringBuilder();
            for(SiteConfiguration.Script l : links) {
                sb.append(String.format("<script src=\"%s\"%s%s%s%s%s</script>",
                    l.url, 
                    l.type != null ? " type=\"" + l.type + "\"" : "",
                    l.crossorigin != null ? " crossorigin=\"" + l.crossorigin + "\"" : "",
                    l.integrity != null ? " integrity=\"" + l.integrity +"\"" : "",
                    l.async ? " async" : "",
                    l.defer ? " defer" : ""));
            }
            return sb.toString();
        }
        
        protected final String createLinks(SiteConfiguration.Style[] links) {
            final StringBuilder sb = new StringBuilder();
            for(SiteConfiguration.Style l : links) {
                sb.append(String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\"%s%s>",
                    l.url, 
                    l.crossorigin != null ? " crossorigin=\"" + l.crossorigin + "\"" : "",
                    l.integrity != null ? " integrity=\"" + l.integrity +"\"" : ""));
            };
            return sb.toString();
        }
    }
}
