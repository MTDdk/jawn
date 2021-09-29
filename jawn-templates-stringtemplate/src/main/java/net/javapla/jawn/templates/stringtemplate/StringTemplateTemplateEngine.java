package net.javapla.jawn.templates.stringtemplate;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STWriter;
import org.stringtemplate.v4.misc.ErrorManager;
import org.stringtemplate.v4.misc.STMessage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.renderers.template.TemplateRendererEngine;
import net.javapla.jawn.core.renderers.template.ViewTemplateLoader;
import net.javapla.jawn.core.renderers.template.ViewTemplates;
import net.javapla.jawn.core.renderers.template.config.Site;
import net.javapla.jawn.core.renderers.template.config.SiteProvider;
import net.javapla.jawn.core.renderers.template.config.TemplateConfig;
import net.javapla.jawn.core.renderers.template.config.TemplateConfigProvider;
import net.javapla.jawn.core.util.AsyncCharArrayWriter;
import net.javapla.jawn.templates.stringtemplate.rewrite.FastSTGroup;

@Singleton
public final class StringTemplateTemplateEngine implements TemplateRendererEngine {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String TEMPLATE_ENDING = ".st";
    
    //private final SiteConfigurationReader configReader;
    private final ViewTemplateLoader templateLoader;
    private final SiteProvider siteProvider;
    
    // The StringTemplateGroup actually handles some sort of caching internally
    private final FastSTGroup/*STFastGroupDir*//*STGroupDir*/ group;
    
    private final boolean useCache;
    private final boolean outputHtmlIndented;
    private final Modes mode;
    
    @Inject
    public StringTemplateTemplateEngine(TemplateConfigProvider<StringTemplateConfiguration> templateConfig,
                                        Modes mode,
                                        //DeploymentInfo info,
                                        ViewTemplateLoader templateLoader,
                                        //SiteConfigurationReader configReader
                                        SiteProvider siteProvider) {
        log.info("Starting the StringTemplateTemplateEngine");
        
        useCache = mode != Modes.DEV;//!conf.isDev(); // TODO should be the responsibility of core (i.e.: ViewTemplateLoader + SiteConfigurationReader)
        outputHtmlIndented = mode != Modes.PROD;//!conf.isProd();
        this.mode = mode;
        
        //this.configReader = configReader;
        this.templateLoader = templateLoader;//new ViewTemplateLoader(info);
        this.siteProvider = siteProvider;
        //templateRootFolder = templateLoader.getTemplateRootFolder();
        
        StringTemplateConfiguration config = new StringTemplateConfiguration();
        
        // Some standard renderers could be added here.. E.g.: HTML escaping
        
        TemplateConfig<StringTemplateConfiguration> stringTemplateConfig = templateConfig.get(); // TODO The TemplateConfig is non-functional at the moment
        if (stringTemplateConfig != null) {
            stringTemplateConfig.init(config);
        }
        
        group = setupTemplateGroup(/*templateRootFolder.toString(),*/ config);
    }

    @Override
    public final byte[] invoke(final Context context, final View view) throws Up.ViewError {
        final long time = System.currentTimeMillis();

        final ViewTemplates viewTemplates = templateLoader.load(view, TEMPLATE_ENDING);
        //writeTemplate(assembleTemplate(context, view, viewTemplates), context.resp().writer());
        
        ST template = assembleTemplate(context, view, viewTemplates);
        
        if (log.isDebugEnabled())
           log.debug("Rendered template: '{}' with layout: '{}' in  {}ms", viewTemplates.templatePath(), viewTemplates.layoutPath(), (System.currentTimeMillis() - time));
        
        return writeTemplate(template);
    }
    
    @Override
    public String invoke(final View view) {
        
        try (final AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            writeTemplate(assembleTemplate(null, view, templateLoader.load(view, TEMPLATE_ENDING)), writer);
            return writer.toString();
        }
    }
    
    private ST assembleTemplate(final Context context, final View view, final ViewTemplates viewTemplates) {
        
        if (! useCache)
            reloadGroup();

        // perhaps layout should always be present, and only template be optional
        
         { // with layout

            final ST template = 
                viewTemplates.templateFound() ? group.getInstanceOf(viewTemplates.templatePath(), viewTemplates.template()) : null;

            ST layout = group.getInstanceOf(viewTemplates.layoutPath(), viewTemplates.layout());
            injectValuesIntoLayoutTemplate(layout, context, template, view);

            return layout;
        }
    }
    
    @Override
    public String getSuffixOfTemplatingEngine() {
        return TEMPLATE_ENDING;
    }

    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.HTML };
    }
    
    private final FastSTGroup setupTemplateGroup(/*String templateRootFolder,*/ StringTemplateConfiguration config) {
            
        // TODO if in production or test
        // when reading the template from disk, do the minification at this point
        // so the STWriter does not have to handle that.
        // (This probably means something like extending STGroup and handle its caching
        // a little differently)
        //boolean minimise = mode != Modes.DEV; // probably just as outputHtmlIndented
        
        FastSTGroup group = new FastSTGroup(templateLoader, /*templateRootFolder,*/ config.delimiterStart, config.delimiterEnd/*, minimise*/);
        group.errMgr = new ErrorManager(new STErrorListener() {
            @Override
            public void runTimeError(STMessage msg) { if (log.isWarnEnabled()) log.warn(msg.toString()); }
            
            @Override
            public void internalError(STMessage msg) { if (log.isWarnEnabled()) log.warn(msg.toString()); }
            
            @Override
            public void compileTimeError(STMessage msg) { if (log.isWarnEnabled()) log.warn(msg.toString()); }
            
            @Override
            public void IOError(STMessage msg) { if (log.isWarnEnabled()) log.warn(msg.toString()); }
        });
        
        // add the user configurations
        config.adaptors.forEach(group::registerModelAdaptor);
        config.renderers.forEach(group::registerRenderer);
        
        return group;
    }
    
    private final void reloadGroup() {
        // clears the internal cache of StringTemplate
        // forces it to read templates from disk
        group.unload();
    }
    
    /** Renders template directly to writer */
    /*private final void writeContentTemplate(final ST contentTemplate, final Writer writer, final Map<String, Object> values) {
        injectTemplateValues(contentTemplate, values);
        
        writeTemplate(contentTemplate, writer);
        //contentTemplate.write(createSTWriter(writer), error);
        //contentTemplate.write(createSTWriter(writer), new Locale(language), error);
    }*/

    /** Renders template into string
     * @return The rendered template if exists, or empty string */
    /*private final String writeContentTemplate(final ST contentTemplate, final Map<String, Object> values, final ErrorBuffer error, boolean inErrorState) {
        if (contentTemplate != null) { // it has to be possible to use a layout without defining a template
            try (Writer sw = new AsyncCharArrayWriter()) {
            
                final ErrorBuffer templateErrors = new ErrorBuffer();
                writeContentTemplate(contentTemplate, sw, values, templateErrors);
                
                // handle potential errors
                if (!templateErrors.errors.isEmpty() && !inErrorState) {
                    for (STMessage err : templateErrors.errors) {
                        if (err.error == ErrorType.INTERNAL_ERROR) {
                            log.warn("Reloading GroupDir as we have found a problem during rendering of template \"{}\"\n{}",contentTemplate.getName(), templateErrors.errors.toString());
                            //README when errors occur, try to reload the specified templates and try the whole thing again
                            // this often rectifies the problem
                            //reloadGroup();
                            //ST reloadedContentTemplate = readTemplate(contentTemplate.getName());
                            //return writeContentTemplate(reloadedContentTemplate, values, error, true);
                        }
                    }
                }
    
                return sw.toString();
            } catch (IOException noMethodsThrowsIOE) { }
        }
        
        // We might just need to render the layout
        // A template is not necessarily needed for that
        return "";
    }*/
    
    private final void injectValuesIntoLayoutTemplate(
            final ST layoutTemplate, final Context ctx, final ST /*String*/ content, 
            final View view) {
        
        injectTemplateValues(layoutTemplate, view.model());
        
        // if the reserved keyword 'site' is not even used in the template, then no need to read anything into it
        // README doesn't work like this..
//        if (layoutTemplate.getAttribute("site") == null) return;
        
//        SiteConfiguration conf = configReader.read(templateRootFolder, controller.length() > 1 && controller.charAt(0) == '/' ? controller.substring(1) : controller, layoutTemplate.impl.prefix.substring(1), useCache);
//        Site site = configReader.retrieveSite(ctx, conf, controller + layoutTemplate.impl.prefix, content, useCache);
        
        Site site;
        if (ctx != null) {
            site = siteProvider.load(ctx, view, content);
        } else {
            // without context, only a simple 'site' can be built
            site =  Site.builder(mode).content(content).build();
        }
        
        // put everything into the reserved keyword
        layoutTemplate.add("site", site);
    }
    
    
    private final void injectTemplateValues(final ST template, final Map<String, Object> values) {
        if (values != null) {
            for (Entry<String, Object> entry : values.entrySet()) {
                try {
                    template.add(entry.getKey(), entry.getValue());
                } catch (IllegalArgumentException ignore) { 
                    /*the value map may contain elements meant for the layout */
                    log.debug("key/value {}/{} not found in template {}", entry.getKey(), entry.getValue(), template); 
                }
            }
        }
    }
    
    private final byte[] writeTemplate(final ST layoutTemplate) {
        try (final AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            
            try {
                layoutTemplate.write(createSTWriter(writer));
            } catch (IOException ignore) {
                // should not actually ever throw anything
            }
            
            return writer.getBytes(StandardCharsets.UTF_8);
        }
    }
    
    private final void writeTemplate(final ST layoutTemplate, final Writer writer/*, final ErrorBuffer error*/) {
        try {
            layoutTemplate.write(createSTWriter(writer));
        } catch (IOException ignore) { 
            // should not actually ever throw anything
        }
    }
    
    private final STWriter createSTWriter(final Writer writer) {
        if (outputHtmlIndented) {
            return new AutoIndentWriter(writer); // README is this even needed when/if STFastGroupDir reads with and without indentation?
        } else {
            return new NoIndentWriter(writer);//no indents for less HTML as a result
        }
    }
    
}
