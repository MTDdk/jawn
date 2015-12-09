package net.javapla.jawn.core.templates.stringtemplate;

class Templates {

    public static final Templates STYLES_TEMPLATE = new Templates(
            "$links:{link|<link rel=\"stylesheet\" type=\"text/css\" href=\"$link$\">}$", "/css/", '$', '$'
    );
    public static final Templates SCRIPTS_TEMPLATE = new Templates(
            "$links:{link|<script src=\"$link$\"></script>}$", "/js/", '$', '$'
    );
            
    
    public final String template;
    public final String prefix;
    public final char delimiterStart;
    public final char delimiterEnd;
    
    public Templates(String template, String prefix, char delimiterStart, char delimitarEnd) {
        this.template = template;
        this.prefix = prefix;
        this.delimiterStart = delimiterStart;
        this.delimiterEnd = delimitarEnd;
    }
}
