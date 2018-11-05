package net.javapla.jawn.core.templates.config;

/**
 * @deprecated is it used ANYWHERE?
 */
@Deprecated
public class Templates {
    
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
