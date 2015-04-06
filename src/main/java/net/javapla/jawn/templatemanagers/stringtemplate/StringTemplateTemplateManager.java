package net.javapla.jawn.templatemanagers.stringtemplate;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import net.javapla.jawn.templatemanagers.TemplateManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STRawGroupDir;
import org.stringtemplate.v4.misc.ErrorBuffer;

public class StringTemplateTemplateManager implements TemplateManager {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String TEMPLATE_DEFAULT = "index.html";
//    private static final String TEMPLATE_ENDING = ".st";
    private static final String TEMPLATES_FOLDER = System.getProperty("resources.templates.folder", "WEB-INF/views/");
    
//    private String templateLocation;
    private ServletContext servletContext;
    private STGroupDir group;
    private ST layoutTemplate;
    
    final StringTemplateConfiguration config;

    public StringTemplateTemplateManager(AbstractStringTemplateConfig templateConfig) {
        STGroupDir.verbose = false;
        Interpreter.trace = false;
        
        config = new StringTemplateConfiguration();
//        AbstractStringTemplateConfig templateConfig = (AbstractStringTemplateConfig) Configuration.getTemplateConfig(getTemplateConfigClass());
        if (templateConfig != null) {
            templateConfig.init(config);
        }
    }
    
    @Override
    public void merge(Map<String, Object> values, String template, Writer writer) {
        merge(values, template, null, "", writer);
    }
    
    @Override
    public void merge(Map<String, Object> values, String template, String layout, String format, Writer writer) {
        merge(values, template, layout, format, "", writer);
    }
    
    //README is it necessary to have it synchronized on the group?
    @Override
    public synchronized void merge(Map<String, Object> values, String template, String layout, String format, String language, Writer writer) {
        if (language == null) language = "";
        
        
        // create new STGroupDir per request, in order to be able to edit HTML without restarting server
//        if (Configuration.activeReload())
//            setupTemplateGroup();
//        else
            group.unload(); // because of reasons: this might be necessary in some cases and completely unnecessary in others
        
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
            readyLayoutTemplate(sw, values, controller, language, error);
        }
            
        renderTemplate(writer, error);
        
        log.info("Rendered template: '{}' with layout: '{}'", template, layout);
        
        if (!error.errors.isEmpty()) {
            log.warn(error.errors.toString());
        }
    }

    @Override
    public synchronized void setServletContext(ServletContext ctx) {
        System.err.println("setServletContext");
        this.servletContext = ctx;
        if (ctx != null) {
//            if (!Configuration.activeReload())
            setupTemplateGroup();
        }
    }

    @Override
    public synchronized void setTemplateLocation(String templateLocation) {
        log.warn("templateLocation {}", templateLocation);
//        this.templateLocation = templateLocation;
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
        contentTemplate.write(new AutoIndentWriter(sw), new Locale(language), error);
    }

    private void readyLayoutTemplate(StringWriter content, Map<String, Object> values, String controller, String language, ErrorBuffer error) {
        injectTemplateValues(layoutTemplate, values);
        
        ConfigurationReader conf = new ConfigurationReader(getTemplateFolder(), controller);
        Site site = new Site();
        
        //add title
        site.title = conf.title();
        
        //add language (if any)
        site.language = language;
        
        //add scripts
        site.scripts = readLinks(Template.SCRIPTS_TEMPLATE, conf.readScripts(), error);
        site.styles = readLinks(Template.STYLES_TEMPLATE, conf.readStyles(), error);
        
        // put the rendered content into the main template
        site.content = content.toString();
        
        // put everything into the reserved keyword
        layoutTemplate.add("site", site);
    }
    
    private void injectTemplateValues(ST template, Map<String, Object> values) {
        for (Entry<String, Object> entry : values.entrySet()) {
            try {
                template.add(entry.getKey(), entry.getValue());
            } catch (IllegalArgumentException ignore) { 
                /*the value map may contain elements meant for the layout */
//                log.debug("key/value {}/{} not found in template {}", entry.getKey(), entry.getValue(), template); 
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
    
    private String getTemplateFolder() {
        return servletContext.getRealPath(TEMPLATES_FOLDER);
    }
    
    private void setupTemplateGroup() {
        group = new STRawGroupDir(getTemplateFolder(), config.delimiterStart, config.delimiterEnd);
        
        // add the user configurations
        config.adaptors.forEach((k, v) -> group.registerModelAdaptor(k, v));
        config.renderers.forEach((k, v) -> group.registerRenderer(k, v));
    }
}
