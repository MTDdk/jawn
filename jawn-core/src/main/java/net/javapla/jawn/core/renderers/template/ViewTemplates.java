package net.javapla.jawn.core.renderers.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

public interface ViewTemplates/*<T>*/ {

    String templateName();
    String layoutName();
    
//    T template();
//    T layout();
    
    Reader templateAsReader() throws FileNotFoundException, IOException;
    Reader layoutAsReader() throws FileNotFoundException, IOException;
}
