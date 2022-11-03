package net.javapla.jawn.core.internal;

import java.io.File;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Renderer;
import net.javapla.jawn.core.Status;

public class ResponseRenderer implements Renderer {
    
    private Map<MediaType, Renderer> renderers = new HashMap<>();
    
    public ResponseRenderer() {
        add(MediaType.PLAIN, this);
        add(MediaType.TEXT, this);
    }
    
    
    public ResponseRenderer add(MediaType type, Renderer renderer) {
        
        if (renderer instanceof Renderer.TemplateRenderer) {
            
        } else {
            renderers.put(type, renderer);
        }
        
        return this;
    }
    
    Renderer renderer(MediaType type) {
        return renderers.getOrDefault(type, this);
    }

    
    // SimpleRenderer
    // MediaType.PLAIN
    @Override
    public byte[] render(Context ctx, Object value) throws Exception {
        
        if (value instanceof Status) {
            ctx.resp().respond((Status) value);
            return null;
        }
        
        if (value instanceof InputStream) {
            ctx.resp().respond((InputStream) value);
            return null;
        }
        
        if (value instanceof FileChannel) {
            ctx.resp().respond((FileChannel) value);
            return null;
        }
        if (value instanceof File) {
            File f = (File) value;
            ctx.resp().contentType(MediaType.byPath(f.getName()));
            ctx.resp().respond(FileChannel.open(f.toPath()));
            return null;
        }
        if (value instanceof Path) {
            Path p = (Path) value;
            ctx.resp().contentType(MediaType.byPath(p.getFileName().toString()));
            ctx.resp().respond(FileChannel.open(p));
            return null;
        }
        
        // String, CharSequence, Number
        ctx.resp().respond(TO_STRING.render(ctx, value));
        return null;//TO_STRING.render(ctx, value);
        
        
        // Java 16
        /*if (value instanceof Status s) {
            ctx.resp().respond(s);
            return null;
        }*/
        
        // Java 17
        /*return switch (value) {
            case CharSequence s -> Renderer.TO_STRING.render(ctx, value);
            default -> Renderer.TO_STRING.render(ctx, value);
        }*/
    }
    
    Renderer TO_STRING = (ctx, value) -> {
        return value.toString().getBytes(ctx.resp().charset());
    };

}
