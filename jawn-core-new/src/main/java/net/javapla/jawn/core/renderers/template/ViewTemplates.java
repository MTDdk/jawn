package net.javapla.jawn.core.renderers.template;

public interface ViewTemplates<T> {

    /*public String templateName;
    public String layoutName;
    
    public T template;
    public T layout;*/
    
    String templateName();
    String layoutName();
    
    T template();
    T layout();
}
