package net.javapla.jawn.core.reflection.forms;


public class InputText extends Input {

    @Attribute
    public String value;
    
    public InputText(String value) {
        super("text");
        this.value = value;
    }
    
}
