package net.javapla.jawn.template.stringtemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.javapla.jawn.core.TemplateRenderer;

public class DeploymentInfo {
    
    private final Path resources;
    
    public DeploymentInfo() {
        this.resources = locateResourcesFolder();
    }
    
    private Path locateResourcesFolder() {
        Path p;
        
        String resourcesFolder = System.getProperty(TemplateRenderer.ENV_RESOURCES_PATH_LOCATION);
        
        if (resourcesFolder != null) {
            p = Paths.get(resourcesFolder);
            if (Files.exists(p)) return p;
        }
        
        p = Paths.get("src","test","resources");
        if (Files.exists(p)) return p;
        
        p = Paths.get("src","main","resources");
        if (Files.exists(p)) return p;
        
        return Path.of("");
    }
    
    public Path resolve(String path) {
        return resources.resolve(path);
    }
    
    public BufferedReader readFile(String path) throws NoSuchFileException {
        Path p = resolve(path);
        return readResolvedFile(p);
    }
    
    public BufferedReader readResolvedFile(Path p) throws NoSuchFileException {
        // look for file on filesystem
        if (Files.exists(p)) {
            try {
                return Files.newBufferedReader(p, StandardCharsets.UTF_8);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
        
        // try reading from resources
        try {
            var stream = getClass().getClassLoader().getResourceAsStream(p.toString()); // gets chain closed by BufferedReader -> InputStreamReader -> InputStream
            return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        throw new NoSuchFileException(p.toString());
    }
    
}
