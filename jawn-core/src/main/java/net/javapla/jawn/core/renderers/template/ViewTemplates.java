package net.javapla.jawn.core.renderers.template;

public interface ViewTemplates {

    String templatePath();
    String layoutPath();
    
    boolean templateFound();
    boolean layoutFound();
    
//    Reader templateAsReader() throws FileNotFoundException, IOException;
//    Reader layoutAsReader() throws FileNotFoundException, IOException;
    
    String template();
    String layout();
}
