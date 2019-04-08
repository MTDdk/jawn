package net.javapla.jawn.core.renderers.template;

public interface ViewTemplates<T> {

    String templateName();
    String layoutName();
    
    T template();
    T layout();
}
