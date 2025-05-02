package net.javapla.jawn.template.stringtemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import net.javapla.jawn.core.TemplateRenderer;

public class ViewTemplateLoader {
    
    private final Path viewsParent;
    
    public ViewTemplateLoader() {
        this.viewsParent = locateViewsParent();
    }
    
    public String loadTemplate(String path) throws NoSuchFileException {
        String p = realPath(path);
        
        // look for file on filesystem
        Path f = viewsParent.resolve(p);
        if (Files.exists(f)) {
            try (var stream = Files.newInputStream(f)) {
                System.out.println("file");
                return readTemplateFromDisk(stream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
        // try reading from resources
        try (var stream = getClass().getClassLoader().getResourceAsStream(p)) {
            System.out.println("stream");
            return readTemplateFromDisk(stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        throw new NoSuchFileException(p);
    }
    
    private String readTemplateFromDisk(InputStream stream) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        }
    }
    
    private String realPath(String path) {
        return TemplateRenderer.DEFAULT_TEMPLATE_PATH + '/' + path;
    }
    
    private Path locateViewsParent() {
        Path p = Paths.get("src","test","resources");
        if (Files.exists(p)) return p;
        
        p = Paths.get("src","main","resources");
        if (Files.exists(p)) return p;
        
        String parent = System.getProperty(TemplateRenderer.ENV_TEMPLATE_PATH_LOCATION);
        if (parent != null) {
            p = Paths.get(parent, TemplateRenderer.DEFAULT_TEMPLATE_PATH);
            if (Files.exists(p)) return p.getParent();
        }
        
        p = Paths.get(TemplateRenderer.DEFAULT_TEMPLATE_PATH);
        if (Files.exists(p)) return p.getParent();
        
        return Path.of("");
    }
    
    /*private char[] readTemplateFromDisk(String path) { // diskReadLayout
        try (var reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8))) {
        //try (var reader = new BufferedReader(new InputStreamReader(new NoNewlineFileInputStream(path), StandardCharsets.UTF_8))) {
        
            
            return reader.lines().collect(Collectors.joining()).toCharArray();
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }*/

}
