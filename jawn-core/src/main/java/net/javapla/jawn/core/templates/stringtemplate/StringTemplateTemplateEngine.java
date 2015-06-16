package net.javapla.jawn.core.templates.stringtemplate;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.PropertiesImpl;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;
import net.javapla.jawn.core.templates.TemplateEngine;
import net.javapla.jawn.core.templates.TemplateEngineHelper;
import net.javapla.jawn.core.templates.config.ConfigurationReader;
import net.javapla.jawn.core.templates.config.Site;
import net.javapla.jawn.core.templates.config.SiteConfiguration;
import net.javapla.jawn.core.templates.config.TemplateConfig;
import net.javapla.jawn.core.templates.config.TemplateConfigProvider;
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
public class StringTemplateTemplateEngine implements TemplateEngine {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    
    private static final String TEMPLATE_ENDING = ".st";
    
    private final StringTemplateConfiguration config;
    private final ConfigurationReader configReader;
    
    // The StringTemplateGroup actually handles some sort of caching internally
    private STGroupDir group;
    
    private final boolean useCache;
    private final boolean outputHtmlIndented;

    @Inject
    public StringTemplateTemplateEngine(TemplateConfigProvider<StringTemplateConfiguration> templateConfig,
                                        ConfigurationReader configReader,
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
        
        useCache = properties.isProd();
        outputHtmlIndented = !properties.isProd();
        
        this.configReader = configReader;
    }

    @Override
    public void invoke(Context context, Response response) throws ViewException {
        invoke(context, response, context.finalizeResponse(response));
    }
    
    @Override
    public void invoke(Context context, Response response, ResponseStream stream) throws ViewException {
        long time = System.currentTimeMillis();
        
        Map<String, Object> values = response.getViewObjects();//context.getViewObjects();
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
      String template = TemplateEngineHelper.getTemplateForResult(context.getRoute(), response);
      String layout = response.layout();
      String language = context.getRouteLanguage();
      
      final ErrorBuffer error = new ErrorBuffer();
      ST contentTemplate = locateTemplate(group, template);

      try (Writer writer = stream.getWriter()) {

          if (layout == null) { // no layout

              renderContentTemplate(contentTemplate, writer, values, language, error);

          } else { // with layout
              String controller = template;
              if (template.charAt(0) == '/')
                  controller = template.substring(1, template.indexOf('/', 1)); // remove leading '/'

              String content = renderContentTemplate(contentTemplate, values, language, error);

              ST layoutTemplate = locateDefaultLayout(group, controller, layout);
              readyLayoutTemplate(layoutTemplate, context, content, values, controller, language, error);

              renderTemplate(layoutTemplate, writer, error);
          }
      } catch (IOException e) {
          throw new ViewException(e);
      }

      log.info("Rendered template: '{}' with layout: '{}' in  {}ms", template, layout, (System.currentTimeMillis() - time));
      
      if (!error.errors.isEmpty()) {
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
    private String getTemplateFolder(Context ctx) {
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
            STGroupDir fallbackGroup = new STRawGroupDir("views", config.delimiterStart, config.delimiterEnd);
            group.importTemplates(fallbackGroup);
        }
        
        // clears the internal cache of StringTemplate
        // forces it to read templates from disk
        if (! useCache)
            group.unload();
    }
    
    private ST locateTemplate(STGroupDir group, String template) {
        return group.getInstanceOf(template);
    }

    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    private ST locateDefaultLayout(STGroupDir group, String controller, String layout) {
        ST index = locateTemplate(group, controller + '/' + layout);
        if (index != null)
            return index;
        index = locateTemplate(group, controller + '/' + TEMPLATE_DEFAULT);
        if (index != null)
            return index;
        return locateTemplate(group, TEMPLATE_DEFAULT);
    }
    
    /** Renders template directly to writer */
    private void renderContentTemplate(ST contentTemplate, Writer writer, Map<String, Object> values, String language, ErrorBuffer error) {
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

    private void readyLayoutTemplate(ST layoutTemplate, Context ctx, String content, Map<String, Object> values, String controller, String language, ErrorBuffer error) {
        injectTemplateValues(layoutTemplate, values);
        
        SiteConfiguration conf = configReader.read(getTemplateFolder(ctx), controller, useCache);
        Site site = new Site(
                    //add title
                    conf.title,
                    
                    //add language (if any)
                    language,
                    
                    //add scripts
                    readLinks(StringTemplateTemplate.SCRIPTS_TEMPLATE, conf.scripts, error),
                    readLinks(StringTemplateTemplate.STYLES_TEMPLATE, conf.styles, error),
                    
                    // put the rendered content into the main template
                    content
                );
        
        // put everything into the reserved keyword
        layoutTemplate.add("site", site);
    }
    
    private void injectTemplateValues(ST template, Map<String, Object> values) {
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
    
    private String readLinks(StringTemplateTemplate template, List<String> links, ErrorBuffer error) {
        if (links.isEmpty()) return null;
        
        ST linkTemplate = new ST(template.template, template.delimiterStart, template.delimiterEnd);
        
        List<String> prefixed = prefixResourceLinks(links, template.prefix);
        
        linkTemplate.add("links", prefixed);
        Writer writer = new StringBuilderWriter();
        linkTemplate.write(createSTWriter(writer), error);
        return writer.toString();
    }

    /**
     * Prefixes local resources with css/ or js/.
     * "Local" is defined by not starting with 'http.*' or 'ftp.*'
     */
    private List<String> prefixResourceLinks(List<String> links, String prefix) {
        return links
                .parallelStream()
                .map(link -> { 
                    if (!(link.matches("^(ht|f)tp.*") || link.startsWith("//")))
                        link = prefix + link; 
                    return link; 
                })
                .collect(Collectors.toList());
    }
    
    private void renderTemplate(ST layoutTemplate, Writer writer, ErrorBuffer error) {
        layoutTemplate.write(createSTWriter(writer), error);
    }
    
    private STWriter createSTWriter(Writer writer) {
        if (outputHtmlIndented) {
            return new AutoIndentWriter(writer);
        } else {
            return new NoIndentWriter(writer);//no indents for less HTML as a result
        }
    }
}
