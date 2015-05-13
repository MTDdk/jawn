package net.javapla.jawn.core.templates.stringtemplate;

class StringTemplateTemplate {

    public static final StringTemplateTemplate STYLES_TEMPLATE = new StringTemplateTemplate(
            "$links:{link|<link rel=\"stylesheet\" type=\"text/css\" href=\"$link$\">\n}$", "/css/", '$', '$'
    );
    public static final StringTemplateTemplate SCRIPTS_TEMPLATE = new StringTemplateTemplate(
            "$links:{link|<script src=\"$link$\"></script>\n}$", "/js/", '$', '$'
    );
            
    
    public final String template;
    public final String prefix;
    public final char delimiterStart;
    public final char delimiterEnd;
    
    public StringTemplateTemplate(String template, String prefix, char delimiterStart, char delimitarEnd) {
        this.template = template;
        this.prefix = prefix;
        this.delimiterStart = delimiterStart;
        this.delimiterEnd = delimitarEnd;
    }
}
