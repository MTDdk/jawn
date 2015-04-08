package net.javapla.jawn.templates;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.Context;
import net.javapla.jawn.ControllerResponse;
import net.javapla.jawn.ResponseStream;
import net.javapla.jawn.exceptions.ViewException;
import net.javapla.jawn.templatemanagers.stringtemplate.StringTemplateConfiguration;
import net.javapla.jawn.templates.configuration.ConfigurationReader;
import net.javapla.jawn.templates.configuration.Site;
import net.javapla.jawn.templates.configuration.SiteConfiguration;
import net.javapla.jawn.templates.configuration.Template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STRawGroupDir;
import org.stringtemplate.v4.misc.ErrorBuffer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StringTemplateTemplateEngine implements TemplateEngine {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String TEMPLATE_DEFAULT = "index.html";
    private static final String TEMPLATE_ENDING = ".st";
    private static final String TEMPLATES_FOLDER = System.getProperty("resources.templates.folder", "WEB-INF/views/");
    
    private final StringTemplateConfiguration config;
    private final ConfigurationReader configReader;
    
    private STGroupDir group;
    private ST layoutTemplate;

    @Inject
    public StringTemplateTemplateEngine(StringTemplateTemplateConfigProvider templateConfig, ConfigurationReader configReader) {
        STGroupDir.verbose = false;
        Interpreter.trace = false;
        
        config = new StringTemplateConfiguration();
        if (templateConfig.get() != null) {
            templateConfig.get().init(config);
        }
        
        this.configReader = configReader;
    }

    @Override
    public void invoke(Context context, ControllerResponse response) {
        invoke(context, response, context.finalize(response));
    }
    
    @Override
    public void invoke(Context context, ControllerResponse response, ResponseStream stream) {
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
      
      //name of the template
      String template = TemplateEngineHelper.getTemplateForResult(context.getRoute(), response);
      String layout = response.layout();
      String language = context.getRouteLanguage();
      
      StringWriter sw = new StringWriter();
      final ErrorBuffer error = new ErrorBuffer();
      
      ST contentTemplate = group.getInstanceOf(template);
      
      if (layout == null) { // no layout
          
          readyContentTemplate(contentTemplate, sw, values, language, error);
          layoutTemplate = contentTemplate;
          
      } else { // with layout
          
          String controller = template.substring(1, template.indexOf('/', 1)); // remove leading '/'
          layoutTemplate = locateDefaultTemplate(group, controller);
          
          if (contentTemplate != null) // it have to be possible to use a layout without defining a template
              readyContentTemplate(contentTemplate, sw, values, language, error);
          readyLayoutTemplate(context, sw, values, controller, language, error);
      }
          
      try (Writer writer = stream.getWriter()) {
          renderTemplate(writer, error);
      } catch (IOException e) {
          throw new ViewException(e);
      }
      
      log.info("Rendered template: '{}' with layout: '{}'", template, layout);
      
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
    
    
    /**
     * Allows to modify the configuration of the template engine.
     * In this case for StringTemplate
     * 
     * @return the StringTemplate configuration
     */
    public StringTemplateConfiguration getConfiguration() {
        return config;
    }

    
    private String getTemplateFolder(Context ctx) {
        return ctx.getRealPath(TEMPLATES_FOLDER);
    }
    
    private void setupTemplateGroup(Context ctx) {
        if (group == null) {
            group = new STRawGroupDir(getTemplateFolder(ctx), config.delimiterStart, config.delimiterEnd);
            
            // add the user configurations
            config.adaptors.forEach((k, v) -> group.registerModelAdaptor(k, v));
            config.renderers.forEach((k, v) -> group.registerRenderer(k, v));
        }
        
        group.unload(); // because of reasons: this might be necessary in some cases and completely unnecessary in others
    }
    
    /**
     * If the default template is found within the controller folder
     * then use this to override the root default template
     */
    private ST locateDefaultTemplate(STGroupDir group, String controller) {
        ST index = group.getInstanceOf(controller + '/' + TEMPLATE_DEFAULT);
        if (index != null)
            return index;
        return group.getInstanceOf(TEMPLATE_DEFAULT);
    }
    
    private void readyContentTemplate(ST contentTemplate, StringWriter sw, Map<String, Object> values, String language, ErrorBuffer error) {
        injectTemplateValues(contentTemplate, values);
        if (language == null)
            contentTemplate.write(new AutoIndentWriter(sw), error);
        else
            contentTemplate.write(new AutoIndentWriter(sw), new Locale(language), error);
    }

    private void readyLayoutTemplate(Context ctx, StringWriter content, Map<String, Object> values, String controller, String language, ErrorBuffer error) {
        injectTemplateValues(layoutTemplate, values);
        
//        ConfigurationReader conf = new ConfigurationReader(getTemplateFolder(ctx), controller);
        SiteConfiguration conf = configReader.read(getTemplateFolder(ctx), controller);
        
        Site site = new Site();
        
        //add title
        site.title = conf.title;
        
        //add language (if any)
        site.language = language;
        
        //add scripts
        site.scripts = readLinks(Template.SCRIPTS_TEMPLATE, conf.scripts, error);
        site.styles = readLinks(Template.STYLES_TEMPLATE, conf.styles, error);
        
        // put the rendered content into the main template
        site.content = content.toString();
        
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
    
    private String readLinks(Template template, List<String> links, ErrorBuffer error) {
        ST linkTemplate = new ST(template.template, template.delimiterStart, template.delimiterEnd);//README should it be ${tag} instead of $tag$?
        
        List<String> prefixed = prefixResourceLinks(links, template.prefix);
        
        linkTemplate.add("links", prefixed);
        StringWriter writer = new StringWriter();
        linkTemplate.write(new AutoIndentWriter(writer), error);//indent writer could be removed for less HTML as a result
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
    
    private void renderTemplate(Writer writer, ErrorBuffer error) {
        layoutTemplate.write(new AutoIndentWriter(writer), error);
    }
}
