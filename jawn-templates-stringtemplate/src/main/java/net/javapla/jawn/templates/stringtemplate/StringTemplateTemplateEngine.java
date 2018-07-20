package net.javapla.jawn.templates.stringtemplate;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STWriter;
import org.stringtemplate.v4.misc.ErrorBuffer;
import org.stringtemplate.v4.misc.ErrorType;
import org.stringtemplate.v4.misc.STMessage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.templates.ContentTemplateLoader;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.core.templates.TemplateEngineHelper;
import net.javapla.jawn.core.templates.config.Site;
import net.javapla.jawn.core.templates.config.SiteConfiguration;
import net.javapla.jawn.core.templates.config.SiteConfigurationReader;
import net.javapla.jawn.core.templates.config.TemplateConfig;
import net.javapla.jawn.core.templates.config.TemplateConfigProvider;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.StringBuilderWriter;
import net.javapla.jawn.templates.stringtemplate.rewrite.STFastGroupDir;

@Singleton
public final class StringTemplateTemplateEngine implements TemplateEngine.TemplateRenderEngine<ST> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String TEMPLATE_ENDING = ".st";
    
    private final SiteConfigurationReader configReader;
    private final ContentTemplateLoader<ST> templateLoader;
    
    // The StringTemplateGroup actually handles some sort of caching internally
    private final STGroupDir group;
    
    private final String templateRootFolder;
    private final boolean useCache;
    private final boolean outputHtmlIndented;
    private final Modes mode;
    
    @Inject
    public StringTemplateTemplateEngine(TemplateConfigProvider<StringTemplateConfiguration> templateConfig,
                                        JawnConfigurations properties,
                                        DeploymentInfo info,
                                        SiteConfigurationReader configReader) {
        log.warn("Starting the StringTemplateTemplateEngine");
        
        STGroupDir.verbose = false;
        Interpreter.trace = false;

        useCache = !properties.isDev();
        outputHtmlIndented = !properties.isProd();
        mode = properties.getMode();
        
        this.configReader = configReader;
        this.templateLoader = new ContentTemplateLoader<>(info, this);
        templateRootFolder = templateLoader.getTemplateRootFolder();
        
        StringTemplateConfiguration config = new StringTemplateConfiguration();
        
        // Add standard renderers
        // TODO Do some HTML escaping by standard
//        config.registerRenderer(String.class, new StringRenderer());
        
        TemplateConfig<StringTemplateConfiguration> stringTemplateConfig = templateConfig.get();
        if (stringTemplateConfig != null) {
            stringTemplateConfig.init(config);
        }
        
        group = setupTemplateGroup(templateRootFolder, config);
    }

    @Override
    public final void invoke(Context context, Result response, ResponseStream stream) throws ViewException {
        long time = 0;
        if (log.isInfoEnabled())
            time = System.currentTimeMillis();

        final Map<String, Object> values = response.getViewObjects();

        if (! useCache)
            reloadGroup();

        //generate name of the template
        final String template = templateLoader.getTemplateForResult(context.getRoute(), response);
        String layout = templateLoader.handleLayoutEndings(response);
        //final String language = null;//context.getRouteLanguage();

        final ErrorBuffer error = new ErrorBuffer();
        final ST contentTemplate = template != null ? readTemplate(template) : null;

        try (final Writer writer = stream.getWriter()) {

            if (layout == null) { // no layout
                // both layout and template should not be null
                if (contentTemplate == null) {
                    if (response.renderable() != null) {
                        writer.append(response.renderable().toString());
                        return;
                    }
                    throw new ViewException("Could not find the template " + contentTemplate + ". Is it spelled correctly?");
                }
                renderContentTemplate(contentTemplate, writer, values, error);

            } else { // with layout

                String content = renderContentTemplate(contentTemplate, values, error, false);

                // Get the calling controller and not just rely on the folder for the template.
                // An action might specify a template that is not a part of the controller.
                final String controller = TemplateEngineHelper.getControllerForResult(context.getRoute());
                
                ST layoutTemplate = templateLoader.locateLayoutTemplate(controller, layout, useCache);
                layout = layoutTemplate.getName(); // for later logging
                injectValuesIntoLayoutTemplate(layoutTemplate, context, content, values, controller);

                renderTemplate(layoutTemplate, writer, error);
            }
        } catch (IOException e) {
            throw new ViewException(e);
        }


        if (log.isInfoEnabled())
            log.info("Rendered template: '{}' with layout: '{}' in  {}ms", template, layout, (System.currentTimeMillis() - time));

        if (!error.errors.isEmpty() && log.isWarnEnabled())
            log.warn(error.errors.toString());
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        return TEMPLATE_ENDING;
    }

    @Override
    public String[] getContentType() {
        return new String[]{MediaType.TEXT_HTML};
    }
    
    
    // It is not necessary to do complex synchronization on this
    // though it may be set a few times, but it will always be set to the same value
    // and by not synchronizing it, we might get some form of performance.
    /*private String realPath;
    private final String getTemplateFolder(final Context ctx) {
        if (realPath == null)
            realPath = ctx.getRealPath(TEMPLATES_FOLDER);
        return realPath;
    }*/
    
    @Override
    public final ST readTemplate(String templatePath) {
        return group.getInstanceOf(templatePath);
    }
    
    @Override
    public ST clone(ST cloneThis) {
        if (cloneThis != null) {
            cloneThis.impl.formalArguments = null; // this is apparently enough for "cloning"
        }
        return cloneThis;
    }
    
    private final STGroupDir setupTemplateGroup(String templateRootFolder, StringTemplateConfiguration config) {
            
        // TODO if in production or test
        // when reading the template from disk, do the minification at this point
        // so the STWriter does not have to handle that.
        // (This probably means something like extending STGroup and handle its caching
        // a little differently)
        boolean minimise = mode != Modes.DEV; // probably just as outputHtmlIndented
        
        STGroupDir group = new STFastGroupDir(templateRootFolder, config.delimiterStart, config.delimiterEnd, minimise);
        
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
    private final void renderContentTemplate(final ST contentTemplate, final Writer writer, final Map<String, Object> values/*, final String language*/, final ErrorBuffer error) {
        injectTemplateValues(contentTemplate, values);
//        if (language == null)
            contentTemplate.write(createSTWriter(writer), error);
//        else
//            contentTemplate.write(createSTWriter(writer), new Locale(language), error);
    }

    /** Renders template into string
     * @return The rendered template if exists, or empty string */
    private final String renderContentTemplate(final ST contentTemplate, final Map<String, Object> values, final ErrorBuffer error, boolean inErrorState) {
        if (contentTemplate != null) { // it has to be possible to use a layout without defining a template
            try (Writer sw = new StringBuilderWriter()) {
            
                final ErrorBuffer templateErrors = new ErrorBuffer();
                renderContentTemplate(contentTemplate, sw, values/*, language*/, templateErrors);
                
                // handle potential errors
                if (!templateErrors.errors.isEmpty() && !inErrorState) {
                    for (STMessage err : templateErrors.errors) {
                        if (err.error == ErrorType.INTERNAL_ERROR) {
                            log.warn("Reloading GroupDir as we have found a problem during rendering of template \"{}\"\n{}",contentTemplate.getName(), templateErrors.errors.toString());
                            //README when errors occur, try to reload the specified templates and try the whole thing again
                            // this often rectifies the problem
                            reloadGroup();
                            ST reloadedContentTemplate = readTemplate(contentTemplate.getName());
                            return renderContentTemplate(reloadedContentTemplate, values, error, true);
                        }
                    }
                }
    
                return sw.toString();
            } catch (IOException noMethodsThrowsIOE) { }
        }
        return "";
    }
    
    protected final void injectValuesIntoLayoutTemplate(
            final ST layoutTemplate, final Context ctx, final String content, 
            final Map<String, Object> values, final String controller) {
        
        injectTemplateValues(layoutTemplate, values);
        
        // if the reserved keyword is not even used in the template, then no need to read anything into it
        // README doesn't work like this..
//        if (layoutTemplate.getAttribute("site") == null) return;
        
        SiteConfiguration conf = configReader.read(templateRootFolder, controller, layoutTemplate.impl.prefix.substring(1), useCache);
        Site site = configReader.retrieveSite(ctx, conf, controller + layoutTemplate.impl.prefix, content, useCache);
        
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
    
    private final void renderTemplate(final ST layoutTemplate, final Writer writer, final ErrorBuffer error) {
        layoutTemplate.write(createSTWriter(writer), error);
    }
    
    private final STWriter createSTWriter(final Writer writer) {
        if (outputHtmlIndented) {
            return new AutoIndentWriter(writer); // README is this even needed when STFastGroupDir reads with and without indentation?
        } else {
            return new NoIndentWriter(writer);//no indents for less HTML as a result
        }
    }
    
}
