package net.javapla.jawn.core;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.renderers.template.TemplateRendererEngine;

public class HttpResponseRenderer implements RendererEngine {

    private ArrayList<RendererEngine> renderers = new ArrayList<>(2);
    
    private LinkedList<TemplateRendererEngine> templateRenderers = new LinkedList<>();
    
    public HttpResponseRenderer add(RendererEngine renderer) {
        if (renderer instanceof TemplateRendererEngine) {
            templateRenderers.add((TemplateRendererEngine) renderer);
        } else {
            renderers.add(renderer);
        }
        return this;
    }

    @Override
    public byte[] invoke(Context context, Object renderable) throws Exception {
        if (renderable instanceof View) {
            if (templateRenderers.size() == 1) {
                TemplateRendererEngine engine = templateRenderers.getFirst();
                if (engine.supports((View) renderable)) {
                    return engine.invoke(context, renderable);
                }
            } else {
                for (TemplateRendererEngine engine : templateRenderers) {
                    if (engine.supports((View) renderable)) {
                        return engine.invoke(context, renderable);
                    }
                }
            }
            throw Up.viewError("You might want to include jawn-templates-stringtemplate or another template engine in your classpath");
        }
        
        
        /** InputStream */
        if (renderable instanceof InputStream) {
            context.resp().send((InputStream) renderable);
            return null;
        }
        
        /** Status */
        if (renderable instanceof Status) {
            context.resp().status(((Status)renderable).value());
            return null;
        }
        
        /** String */
        if (renderable instanceof CharSequence || renderable instanceof Number) {
            return renderable.toString().getBytes(StandardCharsets.UTF_8);
        }
        
        /** raw */
        if (renderable instanceof byte[]) {
            return (byte[]) renderable;
        }
        if (renderable instanceof ByteBuffer) {
            context.resp().send((ByteBuffer)renderable);
            return null;
        }
        
        Iterator<RendererEngine> iterator = renderers.iterator();
        
        
        
        return null;
    }

}
