package net.javapla.jawn.templates.rocker;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.renderers.template.TemplateRendererEngine;

public class RockerTemplateEngine implements TemplateRendererEngine<Object> {
    
    private static final String TEMPLATE_ENDING = ".rocker.html";

    @Override
    public void invoke(Context context, View viewable) {}

    @Override
    public Object readTemplate(String templatePath) {
        return null;
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        return TEMPLATE_ENDING;
    }

    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.HTML };
    }
    

}
