package net.javapla.jawn.template.stringtemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import net.javapla.jawn.core.TemplateRenderer;

public class ViewTemplateLoader {
    
    private final DeploymentInfo info;
    private final Path views;
    
    public ViewTemplateLoader(DeploymentInfo info) {
        this.info = info;
        this.views = locateViewsFolder(info);
        System.out.println(views);
    }
    
    public String loadTemplate(String path) throws NoSuchFileException {
        Path p = realPath(path);
        try (var reader = info.readResolvedFile(p)) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new NoSuchFileException(e.getMessage());
        }
    }

    private Path realPath(String path) {
        //return TemplateRenderer.DEFAULT_TEMPLATE_NAME + '/' + path;
        if (path.charAt(0) == '/') return views.resolve(path.substring(1));
        return views.resolve(path);
    }
    
    private Path locateViewsFolder(DeploymentInfo info) {
        String viewsFolder = System.getProperty(TemplateRenderer.ENV_TEMPLATE_FOLDER_NAME, TemplateRenderer.DEFAULT_TEMPLATE_FOLDER_NAME);
        
        Path p = info.resolve(viewsFolder);
        if (Files.exists(p)) return p;
        
        return Path.of("");
    }
    
    /*public String loadTemplate(String path) throws NoSuchFileException {
        Path p = realPath(path);
        
        // look for file on filesystem
        //Path f = viewsParent.resolve(p);
        if (Files.exists(p)) {
            try (var stream = Files.newInputStream(p)) {
                System.out.println("file " + p.toString());
                return readTemplateFromDisk(stream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
        // try reading from resources
        try (var stream = getClass().getClassLoader().getResourceAsStream(p.toString())) {
            System.out.println("stream " + p.toString());
            return readTemplateFromDisk(stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        throw new NoSuchFileException(p.toString());
    }
    
    private String readTemplateFromDisk(InputStream stream) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }*/
    
    /*private Path locateViewsFolder() {
        Path p;
        
        String parent      = System.getProperty(TemplateRenderer.ENV_RESOURCES_PATH_LOCATION);
        String viewsFolder = System.getProperty(TemplateRenderer.ENV_TEMPLATE_PATH_NAME, TemplateRenderer.DEFAULT_TEMPLATE_NAME);
        
        if (parent != null) {
            p = Paths.get(parent, viewsFolder);
            if (Files.exists(p)) return p;
        }
        
        p = Paths.get("src","test","resources", viewsFolder);
        if (Files.exists(p)) return p;
        
        p = Paths.get("src","main","resources", viewsFolder);
        if (Files.exists(p)) return p;
        
        
        p = Paths.get(viewsFolder);
        if (Files.exists(p)) return p;
        
        return Path.of("");
    }*/
    
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

    //private record ViewPathTuple(Path parent, Path views) {}
}
