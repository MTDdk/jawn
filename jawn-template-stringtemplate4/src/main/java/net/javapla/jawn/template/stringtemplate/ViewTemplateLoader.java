package net.javapla.jawn.template.stringtemplate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ViewTemplateLoader {
    
    public ViewTemplateLoader() {}
    
    public String loadTemplate(String path) {
        return new String(readTemplateFromDisk(path));
    }
    
    private char[] readTemplateFromDisk(String path) { // diskReadLayout
        try (var reader = new BufferedReader(new InputStreamReader(new NoNewlineFileInputStream(path), StandardCharsets.UTF_8))) {
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
