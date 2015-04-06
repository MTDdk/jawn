package net.javapla.jawn.templatemanagers.stringtemplate;

import java.util.HashMap;
import java.util.Map;

import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.ModelAdaptor;

public class StringTemplateConfiguration {

    //TODO make all fields package private
    public Map<Class<?>, AttributeRenderer> renderers = new HashMap<>();
    public Map<Class<?>, ModelAdaptor> adaptors = new HashMap<>();
    
    public char delimiterStart = '$';
    public char delimiterEnd = '$';
    
    public void registerRenderer(Class<?> attributeType, AttributeRenderer r) {
        renderers.put(attributeType, r);
    }
    public void registerModelAdaptor(Class<?> attributeType, ModelAdaptor adaptor) {
        adaptors.put(attributeType, adaptor);
    }
    
    public void setDelimiterEnd(char end) {
        delimiterEnd = end;
    }
    public void setDelimiterStart(char start) {
        delimiterStart = start;
    }
}
