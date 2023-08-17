package net.javapla.jawn.core.internal.template;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.TemplateRenderer;

public class LoxTemplateRenderer implements TemplateRenderer {
    
    
    private final Path basedir;

    public LoxTemplateRenderer() {
        String viewsPath = System.getProperty(ENV_TEMPLATE_PATH_KEY, DEFAULT_TEMPLATE_PATH);
        basedir = Paths.get(viewsPath).resolve("views");
        System.out.println(getClass() + " --  " + Files.exists(basedir));
    }

    @Override
    public byte[] render(Context ctx, Template template) throws IOException {
        
        Path view = basedir.resolve(template.viewName);
        //BufferedReader reader = Files.newBufferedReader(view, StandardCharsets.UTF_8);
        String source = Files.readString(view, StandardCharsets.UTF_8);
        
        return source.getBytes();
    }

}
