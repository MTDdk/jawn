package net.javapla.jawn.core.internal;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Parser;
import net.javapla.jawn.core.Renderer;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;

public class ParserRenderEngine implements Parser.ParserProvider {
    
    private final Renderer SIMPLE_RENDERER = new SimpleRenderer();
    
    private final Map<MediaType, Parser>   parsers = new HashMap<>();
    private final Map<MediaType, Renderer> renderers = new HashMap<>();
    
    public ParserRenderEngine() {
        //add(MediaType.PLAIN, this);
        //add(MediaType.TEXT, this);
    }
    

    public ParserRenderEngine add(MediaType type, Parser parser) {
        parsers.put(type, parser);
        return this;
    }
    
    public ParserRenderEngine add(MediaType type, Renderer renderer) {
        if (renderer instanceof Renderer.TemplateRenderer) {
            
        } else {
            renderers.put(type, renderer);
        }
        
        return this;
    }
    
    
    public Parser get(MediaType type) {
        return parsers.getOrDefault(type, (ctx, t) -> {throw Up.UnsupportedMediaType(type.name());});
    }
    Renderer render(MediaType type) {
        return renderers.getOrDefault(type, SIMPLE_RENDERER);
    }
    
    
    // SimpleRenderer
    // MediaType.PLAIN
    final class SimpleRenderer implements Renderer {
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
            
            /** File */
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
            
            /** bytes */
            if (value instanceof ByteBuffer) {
                ctx.resp().respond((ByteBuffer) value);
                return null;
            }
            
            /*Renderer r = renderers.get(ctx.resp().contentType());
            if (r != null) return r.render(ctx, value);*/
            
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
    
}
