package net.javapla.jawn.templatemanagers.stringtemplate;

import java.util.Locale;

import org.stringtemplate.v4.AttributeRenderer;

public class StringRenderer implements AttributeRenderer {

    @Override
    public String toString(Object o, String formatString, Locale locale) {
        // o will be instanceof String
        if ( formatString==null ) return o.toString();
        
        if ("image".endsWith(formatString))
            return "static/images/"+o.toString();
        
        return o.toString();
    }

}
