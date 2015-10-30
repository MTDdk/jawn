package net.javapla.jawn.core.templates.stringtemplate;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.core.templates.TemplateEngineHelper;
import net.javapla.jawn.core.templates.config.Site;
import net.javapla.jawn.core.templates.config.SiteConfiguration;
import net.javapla.jawn.core.templates.config.SiteConfigurationReader;
import net.javapla.jawn.core.templates.config.TemplateConfig;
import net.javapla.jawn.core.templates.config.TemplateConfigProvider;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.core.util.StringBuilderWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STRawGroupDir;
import org.stringtemplate.v4.STWriter;
import org.stringtemplate.v4.misc.ErrorBuffer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class StringTemplateTemplateEngine implements TemplateEngine {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    
    private static final String TEMPLATE_ENDING = ".st";
    
    private final StringTemplateConfiguration config;
    private final SiteConfigurationReader configReader;
    
    // The StringTemplateGroup actually handles some sort of caching internally
    private STGroupDir group;
    
    private final boolean useCache;
    private final boolean outputHtmlIndented;
    private final Modes mode;

    @Inject
    public StringTemplateTemplateEngine(TemplateConfigProvider<StringTemplateConfiguration> templateConfig,
                                        SiteConfigurationReader configReader,
                                        PropertiesImpl properties) {
        log.warn("Starting the StringTemplateTemplateEngine");
        
        STGroupDir.verbose = false;
        Interpreter.trace = false;

        
        config = new StringTemplateConfiguration();
        
        // Add standard renderers
        // TODO Do some HTML escaping by standard
//        config.registerRenderer(String.class, new StringRenderer());
        
        TemplateConfig<StringTemplateConfiguration> stringTemplateConfig = templateConfig.get();
        if (stringTemplateConfig != null) {
            stringTemplateConfig.init(config);
        }
        
        useCache = !properties.isDev();
        outputHtmlIndented = !properties.isProd();
        mode = properties.getMode();
        
        this.configReader = configReader;
    }

    @Override
    public final void invoke(Context context, Response response) throws ViewException {
        invoke(context, response, context.finalizeResponse(response));
    }
    
    @Override
    public final void invoke(Context context, Response response, ResponseStream stream) throws ViewException {
        long time = System.currentTimeMillis();
        
        final Map<String, Object> values = response.getViewObjects();//context.getViewObjects();
//      Object renderable = response.renderable();
      /*if (renderable == null) {
          values = new HashMap<>(); // just use empty map
      } else if (renderable instanceof Map) {
          values = (Map<String, Object>) renderable;
      } else {
          // We are getting an arbitrary Object and put that into
          // the root of the template engine
          
          // If you are rendering something like Results.ok().render(new MyObject()) //TODO rephrase
          // Assume MyObject has a public String name field.            
          // You can then access the fields in the template like that:
          // ${myObject.publicField}            
          
          String realClassNameLowerCamelCase = StringUtil.decapitalize(renderable.getClass().getSimpleName());
          
          values = new HashMap<>();
          values.put(realClassNameLowerCamelCase, renderable);
      }*/
      
      setupTemplateGroup(context);
      
      //generate name of the template
      final String template = TemplateEngineHelper.getTemplateForResult(context.getRoute(), response);
      String layout = response.layout();
      String language = null;//context.getRouteLanguage();
      
      final ErrorBuffer error = new ErrorBuffer();
      final ST contentTemplate = locateTemplate(group, template);

      try (final Writer writer = stream.getWriter()) {

          if (layout == null) { // no layout
              // both layout and template cannot be null
              if (contentTemplate == null) throw new ViewException("Could not find the template " + contentTemplate + ". Is it spelled correctly?");
              renderContentTemplate(contentTemplate, writer, values, language, error);
              
          } else { // with layout
              // Get the calling controller and not just rely on the folder for the template.
              // An action might specify a template that is not a part of the controller.
              String controller = TemplateEngineHelper.getControllerForResult(context.getRoute());

              String content = renderContentTemplate(contentTemplate, values, language, error);

              ST layoutTemplate = locateDefaultLayout(group, controller, layout);
              if (layoutTemplate == null) throw new ViewException("index.html.st is not to be found anywhere");
              
              layout = layoutTemplate.getName(); // for later logging
              readyLayoutTemplate(layoutTemplate, context, content, values, controller, language, error);

              renderTemplate(layoutTemplate, writer, error);
          }
      } catch (IOException e) {
          throw new ViewException(e);
      }

      
      if (log.isInfoEnabled())
      log.info("Rendered template: '{}' with layout: '{}' in  {}ms", template, layout, (System.currentTimeMillis() - time));
      
      if (!error.errors.isEmpty() && log.isErrorEnabled()) {
          log.warn(error.errors.toString());
      }
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
    private String realPath;
    private final String getTemplateFolder(final Context ctx) {
        if (realPath == null)
            realPath = ctx.getRealPath(TEMPLATES_FOLDER);
        return realPath;
    }
    
    private void setupTemplateGroup(Context ctx) {
        if (group == null) {
            group = new STRawGroupDir(getTemplateFolder(ctx), config.delimiterStart, config.delimiterEnd);
            
            // add the user configurations
            config.adaptors.forEach((k, v) -> group.registerModelAdaptor(k, v));
            config.renderers.forEach((k, v) -> group.registerRenderer(k, v));
            
            // adding a fallback group that will try to serve from resources instead of webapp/WEB-INF/
//            STGroupDir fallbackGroup = new STRawGroupDir("views", config.delimiterStart, config.delimiterEnd);
//            group.importTemplates(fallbackGroup);
        }
        
        // clears the internal cache of StringTemplate
        // forces it to read templates from disk
        if (! useCache)
            group.unload();
        //README apparently this *might* be called multiple times during first reads (maybe even other times as well),
        // resulting in a ConcurrentModicifationException from the ArrayList internal of StringTemplate.
        // Something needs to be done about this.
    }
    
    private final ST locateTemplate(final STGroupDir group, final String template) {
        return group.getInstanceOf(template);
    }

    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    private final ST locateDefaultLayout(final STGroupDir group, String controller, final String layout) {
        // the exact path is defined in the controller
//        ST index = locateTemplate(group, layout);
//        if (index != null)
//            return index;
        
        // look for a layout of a given name within the controller folder
        ST index = locateTemplate(group, controller + '/' + layout);
        if (index != null)
            return index;
        
        
        // going for defaults
        // look for the default layout in controller folder
        // and bubble up one folder if nothing is found
        while ((index = locateTemplate(group, controller + '/' + LAYOUT_DEFAULT)) == null && controller.indexOf('/') > 0) {
            controller = controller.substring(0,controller.lastIndexOf('/'));
        }
//        return index;
        /*
        index = locateTemplate(group, controller + '/' + TEMPLATE_DEFAULT);
        if (index != null)
            return index;
        
        // look exactly one folder up
        index = locateTemplate(group, controller.substring(controller.indexOf('/')) + '/' + TEMPLATE_DEFAULT);*/
        if (index != null)
            return index;
        
        // last resort - this should always be present
        return locateTemplate(group, LAYOUT_DEFAULT);
    }
    
    /** Renders template directly to writer */
    private final void renderContentTemplate(final ST contentTemplate, final Writer writer, final Map<String, Object> values, final String language, final ErrorBuffer error) {
        injectTemplateValues(contentTemplate, values);
        if (language == null)
            contentTemplate.write(createSTWriter(writer), error);
        else
            contentTemplate.write(createSTWriter(writer), new Locale(language), error);
    }

    /** Renders template into string
     * @return The rendered template if exists, or empty string */
    private String renderContentTemplate(ST contentTemplate, Map<String, Object> values, String language, final ErrorBuffer error) {
        if (contentTemplate != null) { // it has to be possible to use a layout without defining a template
            Writer sw = new StringBuilderWriter();
            renderContentTemplate(contentTemplate, sw, values, language, error);
            return sw.toString();
        }
        return "";
    }

    private final void readyLayoutTemplate(
            final ST layoutTemplate, final Context ctx, final String content, 
            final Map<String, Object> values, final String controller, final String language, final ErrorBuffer error) {
        injectTemplateValues(layoutTemplate, values);
        
        SiteConfiguration conf = 
                configReader.read(getTemplateFolder(ctx), controller, layoutTemplate.impl.prefix.substring(1), useCache);
        Site site = new Site(
            // add the URL
            ctx.requestUrl(),
                
            // add title
            conf.title,
            
            // add language (if any)
            language,
            
            //add scripts
            readLinks(StringTemplateTemplate.SCRIPTS_TEMPLATE, conf.scripts, error),
            readLinks(StringTemplateTemplate.STYLES_TEMPLATE, conf.styles, error),
            
            // put the rendered content into the main template
            content,
            
            // state the current mode
            mode
        );
        
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
    //                log.debug("key/value {}/{} not found in template {}", entry.getKey(), entry.getValue(), template); 
                }
            }
        }
    }
    
    /*private String readLinks(StringTemplateTemplate template, List<String> links, ErrorBuffer error) {
        if (links.isEmpty()) return null;
        
        final ST linkTemplate = new ST(template.template, template.delimiterStart, template.delimiterEnd);
        
        List<String> prefixed = prefixResourceLinks(links, template.prefix);
        
        linkTemplate.add("links", prefixed);
        final Writer writer = new StringBuilderWriter();
        linkTemplate.write(createSTWriter(writer), error);
        return writer.toString();
    }*/
    private final String readLinks(final StringTemplateTemplate template, final String[] links, final ErrorBuffer error) {
        if (links == null) return null;
        
        final ST linkTemplate = new ST(template.template, template.delimiterStart, template.delimiterEnd);
        
        linkTemplate.add("links", prefixResourceLinks(links, template.prefix));
        
        final Writer writer = new StringBuilderWriter();
        linkTemplate.write(createSTWriter(writer), error);
        return writer.toString();
    }

    /**
     * Prefixes local resources with css/ or js/.
     * "Local" is defined by not starting with 'http.*' or 'ftp.*'
     */
    /*private List<String> prefixResourceLinks(List<String> links, String prefix) {
        return links
                .parallelStream()
                .map(link -> { 
                    if (!(link.matches("^(ht|f)tp.*") || link.startsWith("//")))
                        link = prefix + link; 
                    return link; 
                })
                .collect(Collectors.toList());
    }*/
    private /*List<String>*/String[] prefixResourceLinks(final String[] links, final String prefix) {
        
        return Arrays.stream(links).parallel()
//        return links
//                .parallelStream()
                .map(link -> { 
                    if (!(link.matches("^(ht|f)tp.*") || link.startsWith("//")))
                        link = prefix + link; 
                    return link; 
                })
                .toArray(String[]::new);
                //.collect(Collectors.toList());
    }
    
    private final void renderTemplate(final ST layoutTemplate, final Writer writer, final ErrorBuffer error) {
        layoutTemplate.write(createSTWriter(writer), error);
    }
    
    private final STWriter createSTWriter(final Writer writer) {
        if (outputHtmlIndented) {
            return new AutoIndentWriter(writer);
        } else {
            return new NoIndentWriter(writer);//no indents for less HTML as a result
        }
    }
}
