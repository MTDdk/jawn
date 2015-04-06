package net.javapla.jawn.templatemanagers.stringtemplate.forms;

import java.util.ArrayList;
import java.util.List;

public class Form extends HtmlElement {
    
    @Attribute
    protected String role;
    @Attribute
    protected String method;
    @Attribute
    protected String action;
    @Attribute
    @AttributeName(name = "accept-charset")
    protected String accept_charset;
    
    protected List<HtmlElement> elements;
    
    public Form() {
        this.elements = new ArrayList<>();
    }
    
    public Form role(String role) {
        this.role = role;
        return this;
    }
    public Form method(String method) {
        this.method = method;
        return this;
    }
    public Form action(String action) {
        this.action = action;
        return this;
    }
    public Form acceptCharset(String charset) {
        this.accept_charset = charset;
        return this;
    }
    
    public void addHtmlElement(HtmlElement element) {
        elements.add(element);
    }
    
    @Override
    public String toString() {
        StringBuilder bob = new StringBuilder();
        bob.append("<form ");
        
        bob.append(attributes());
        bob.append('>');
        
        for (HtmlElement htmlElement : elements) {
            bob.append(htmlElement);
        }
        
        bob.append("</form>"); //add tag end
        
        return bob.toString();
    }
}
