package net.javapla.jawn.template.stringtemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.ErrorManager;
import org.stringtemplate.v4.misc.STMessage;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.TemplateRenderer;
import net.javapla.jawn.template.stringtemplate.rewrite.FastSTGroup;

public class StringTemplateTemplateRenderer implements TemplateRenderer {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final FastSTGroup group;
    
    public StringTemplateTemplateRenderer(ViewTemplateLoader templateLoader) {
        
        this.group = createTemplateGroup(templateLoader, new StringTemplateConfiguration());
        
    }

    @Override
    public byte[] render(Context ctx, Template template) throws Exception {
        return null;
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
