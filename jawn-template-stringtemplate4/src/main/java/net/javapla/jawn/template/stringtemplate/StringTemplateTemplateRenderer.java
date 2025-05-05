package net.javapla.jawn.template.stringtemplate;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.ErrorManager;
import org.stringtemplate.v4.misc.STMessage;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.TemplateRenderer;
import net.javapla.jawn.core.View;
import net.javapla.jawn.template.stringtemplate.rewrite.FastSTGroup;

public class StringTemplateTemplateRenderer implements TemplateRenderer {
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());
    
    private final FastSTGroup group;
    private final ViewTemplateLoader templateLoader;
    
    public StringTemplateTemplateRenderer(ViewTemplateLoader templateLoader) {
        
        this.templateLoader = templateLoader;
        this.group = createTemplateGroup(templateLoader, new StringTemplateConfiguration());
        
    }

    @Override
    public byte[] render(Context ctx, View template) throws Exception {
        long time = System.currentTimeMillis();
        
        ST view = group.getInstanceOf(template.view(), templateLoader.loadTemplate(template.view() + FastSTGroup.TEMPLATE_FILE_EXTENSION));
        
        inject(view, template);
        
        // see if a key is used in the template
        //if (view.impl.formalArguments != null) System.out.println(view.impl.formalArguments.containsKey("methodname"));
        
        log.debug("Rendered template {} in {}ms", template.view(), (System.currentTimeMillis() - time));
        return view.render().getBytes(StandardCharsets.UTF_8);
    }
    
    private void inject(ST view, View template) {
        template.data().forEach((key, val) -> {
            try {
                view.add(key, val);
            } catch (IllegalArgumentException ignore) {
                log.debug("key/value {}/{} not found in template {}", key, val, template.view());
            }
        });
    }

    
    private FastSTGroup createTemplateGroup(ViewTemplateLoader templateLoader,StringTemplateConfiguration configuration) {
        
        FastSTGroup group = new FastSTGroup(templateLoader, configuration.delimiterStart, configuration.delimiterEnd);
        group.errMgr = new ErrorManager(new STErrorListener() {
            @Override
            public void runTimeError(STMessage msg) { log.warn(msg.toString()); }
            
            @Override
            public void internalError(STMessage msg) { log.warn(msg.toString()); }
            
            @Override
            public void compileTimeError(STMessage msg) { log.warn(msg.toString()); }
            
            @Override
            public void IOError(STMessage msg) { log.warn(msg.toString()); }
        });
        
        return group;
    }
}
