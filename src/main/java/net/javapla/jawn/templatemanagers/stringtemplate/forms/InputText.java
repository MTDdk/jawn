package net.javapla.jawn.templatemanagers.stringtemplate.forms;


public class InputText extends Input {

    @Attribute
    public String value;
    
    public InputText(String value) {
        super("text");
        this.value = value;
    }
    
}
