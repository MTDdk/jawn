package net.javapla.jawn.templatemanagers.stringtemplate.forms;

public class Label extends HtmlElement {

    @Attribute
    @AttributeName(name = "for")
    protected String forElement;

    protected String text;
    
    public Label(String forElement, String text) {
        this.forElement = forElement;
        this.text = text;
    }
    
    public Label(HtmlElement forElement, String text) {
        this.forElement = forElement.id;
        this.text = text;
    }
    
    @Override
    public String toString() {
        StringBuilder bob = new StringBuilder();
        bob.append("<label ");
        
        bob.append(attributes());
        bob.append('>');
        bob.append(text);
        bob.append("</label>"); //add tag end
        
        return bob.toString();
    }
}
