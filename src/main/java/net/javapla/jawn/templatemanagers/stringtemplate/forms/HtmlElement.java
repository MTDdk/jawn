package net.javapla.jawn.templatemanagers.stringtemplate.forms;

import java.lang.reflect.Field;

public class HtmlElement {

    @Attribute
    protected String id;
    @Attribute
    protected String cssClass;
    
    
    public HtmlElement id(String id) {
        this.id = id;
        return this;
    }
    public HtmlElement cssClass(String cssClass) {
        this.cssClass = cssClass;
        return this;
    }
    
    protected String attributes() {
        StringBuilder bob = new StringBuilder();
        
        if (id != null) {
            bob.append("id=\"");
            bob.append(id);
            bob.append("\" ");
        }
        if (cssClass != null) {
            bob.append("class=\"");
            bob.append(cssClass);
            bob.append("\" ");
        }
        
        
        // provides the means for extendability
        Class<? extends HtmlElement> clazz = this.getClass();
        
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Attribute.class)) continue;
            
            try {
                Object value = field.get(this);
                if (value == null || !(value instanceof String))
                    continue;
                
                AttributeName name = field.getAnnotation(AttributeName.class);
                if (name == null)
                    bob.append(field.getName());
                else 
                    bob.append(name.name());
                bob.append("=\"");
                bob.append(value);
                bob.append("\" ");
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (bob.length() > 1)
            bob.deleteCharAt(bob.length()-1); //remove space
        
        return bob.toString();
    }
}
