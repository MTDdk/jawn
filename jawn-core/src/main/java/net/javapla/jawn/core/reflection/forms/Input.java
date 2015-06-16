package net.javapla.jawn.core.reflection.forms;


public class Input extends HtmlElement {
    
    @Attribute
    protected final String type;
    @Attribute
    protected String name;
    
    public Input(String type) {
        this.type = type;
    }
    
    public Input name(String name) {
        this.name = name;
        return this;
    }
    

    @Override
    public String toString() {
        StringBuilder bob = new StringBuilder();
        bob.append("<input type=\""+type+"\" ");
        
        
        bob.append(attributes());
        
        bob.append('>'); //add tag end
        
        return bob.toString();
    }
}
