package net.javapla.jawn.templatemanagers.stringtemplate;

//TODO make package private
public class Template {

    public static final Template STYLES_TEMPLATE = new Template(
            "$links:{link|<link rel=\"stylesheet\" type=\"text/css\" href=\"$link$\">\n}$", "/css/", '$', '$'
    );
    public static final Template SCRIPTS_TEMPLATE = new Template(
            "$links:{link|<script src=\"$link$\"></script>\n}$", "/js/", '$', '$'
    );
            
    
    public final String template;
    public final String prefix;
    public final char delimiterStart;
    public final char delimiterEnd;
    
    public Template(String template, String prefix, char delimiterStart, char delimitarEnd) {
        this.template = template;
        this.prefix = prefix;
        this.delimiterStart = delimiterStart;
        this.delimiterEnd = delimitarEnd;
    }
}
