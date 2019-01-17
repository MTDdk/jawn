package net.javapla.jawn.core.templates.config;

import net.javapla.jawn.core.util.Modes;

public class Site {

    public final String url,
                        title,
                        scripts,
                        styles;
    
    public String content;
    
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
    
    public Site content(final String content) {
        this.content = content;
        return this;
    }
    
    public static Site.Builder builder(final Modes mode) {
        return new Site.Builder(mode);
    }
    
    public static class Builder {
        public String 
            url,
            title,
            scripts,
            styles;

        public String content;

        public final Modes mode;
        private Builder(final Modes mode) { this.mode = mode; }
        
        public Site.Builder url(String url) {
            this.url = url;
            return this;
        }
        public Site.Builder title(String title) {
            this.title = title;
            return this;
        }
        public Site.Builder scripts(String scripts) {
            this.scripts = scripts;
            return this;
        }
        public Site.Builder scripts(SiteConfiguration.Tag[] links) {
            if (links != null)
                this.scripts = createScripts(links);
            return this;
        }
        public Site.Builder styles(String styles) {
            this.styles = styles;
            return this;
        }
        public Site.Builder styles(SiteConfiguration.Tag[] links) {
            if (links != null)
                this.styles = createStyles(links);
            return this;
        }
        public Site.Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Site build() {
            return new Site(url, title, scripts, styles, content, mode);
        }
        
        protected final String createScripts(SiteConfiguration.Tag[] links) {
            return createLinks(links, "<script src=\"", "></script>");
        }
        
        protected final String createStyles(SiteConfiguration.Tag[] links) {
            return createLinks(links, "<link rel=\"stylesheet\" type=\"text/css\" href=\"", ">");
        }
        
        protected final String createLinks(SiteConfiguration.Tag[] links, String prefix, String postfix) {
            // We are using a StringBuilder extensively and sacrificing readability a lot,
            // but String#format and "String + String"-construct use StringBuilder internally, 
            // so we could just as well minimise the overhead
            final StringBuilder sb = new StringBuilder();
            for(SiteConfiguration.Tag l : links) {
                sb.append(prefix);
                sb.append(l.url);
                sb.append("\"");
                l.attr.entrySet().forEach(entry -> {
                    sb.append(" ");
                    sb.append(entry.getKey());
                    sb.append("=\"");
                    sb.append(entry.getValue());
                    sb.append("\"");
                });
                
                sb.append(postfix);
                
                if (mode == Modes.DEV) {
                    sb.append('\n');
                }
            };
            return sb.toString();
        }
    }
}
