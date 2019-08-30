package net.javapla.jawn.core.renderers.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

public interface ViewTemplates {

    String templatePath();
    String layoutPath();
    
    boolean templateFound();
    boolean layoutFound();
    
    Reader templateAsReader() throws FileNotFoundException, IOException;
    Reader layoutAsReader() throws FileNotFoundException, IOException;
}
