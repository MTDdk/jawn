package net.javapla.jawn.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class TemplateEngineManagerImpl implements TemplateEngineManager {
    private final Logger log = LoggerFactory.getLogger(getClass());

    
    // Keep a reference of providers rather than instances, so template engines
    // don't have to be singleton if they don't want
    private final Map<String, Provider<? extends TemplateEngine>> contentTypeToTemplateEngineMap;
    
    @Inject
    public TemplateEngineManagerImpl(Provider<JsonTemplateEngine>           json,
                                     Provider<XmlTemplateEngine>            xml,
                                     Provider<TextTemplateEngine>           text,
                                     Provider<StreamTemplateEngine>         stream,
                                     Provider<StringTemplateTemplateEngine> html) {
        
        Map<String, Provider<? extends TemplateEngine>> map = 
                                        new HashMap<String, Provider<? extends TemplateEngine>>();
        
        // Map built in content type bindings
        map.put(json.get().getContentType(), json);
        map.put(xml.get().getContentType(), xml);
        map.put(text.get().getContentType(), text);
        map.put(stream.get().getContentType(), stream);
        map.put(html.get().getContentType(), html);
        
        
        this.contentTypeToTemplateEngineMap = map; //README probably some immutable
        
        //TODO log we have been initiated
    }
    
    @Override
    public Set<String> getContentTypes() {
        return contentTypeToTemplateEngineMap.keySet();// TODO immutable
    }

    @Override
    public TemplateEngine getTemplateEngineForContentType(String contentType) {
        Provider<? extends TemplateEngine> provider = 
                                            contentTypeToTemplateEngineMap.get(contentType);

        if (provider != null) {
            log.info("Found template engine for type: {}", contentType);
            return provider.get();
        } else {
            log.warn("Did not find any engine for type: {}", contentType);
            return null;
        }
    }

}
