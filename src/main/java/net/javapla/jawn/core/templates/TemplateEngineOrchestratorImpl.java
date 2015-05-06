package net.javapla.jawn.core.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.javapla.jawn.core.templates.stringtemplate.StringTemplateTemplateEngine;
import net.javapla.jawn.core.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class TemplateEngineOrchestratorImpl implements TemplateEngineOrchestrator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    
    // Keep a reference of providers rather than instances, so template engines
    // don't have to be singleton if they don't want
    private final Map<String, Provider<? extends TemplateEngine>> contentTypeToTemplateEngineMap;
    
    @Inject
    public TemplateEngineOrchestratorImpl(Provider<JsonTemplateEngine>      json,
                                     Provider<XmlTemplateEngine>            xml,
                                     Provider<TextTemplateEngine>           text,
                                     Provider<StreamTemplateEngine>         stream,
                                     Provider<ImageTemplateEngine>          image,
                                     Provider<StringTemplateTemplateEngine> html,
                                     Injector injector) {
        
        Map<String, Provider<? extends TemplateEngine>> map = 
                                        new HashMap<String, Provider<? extends TemplateEngine>>();
        
        // Map built in content type bindings
        mapEngine(map, json);
        mapEngine(map, xml);
        mapEngine(map, text);
        mapEngine(map, stream);
        mapEngine(map, image);
        mapEngine(map, html);
        
        
        // Find any other defined bindings for TemplateEngine,
        // and put them into the map.
        // If a TemplateEngine handles the same content types
        // as already in the map, the existing will be overridden
        for (Entry<Key<?>, Binding<?>> binding : injector.getBindings().entrySet()) {
            if (TemplateEngine.class.isAssignableFrom(binding.getKey().getTypeLiteral().getRawType())) {
                @SuppressWarnings("unchecked")
                Provider<? extends TemplateEngine> provider = 
                    (Provider<? extends TemplateEngine>) binding.getValue().getProvider();
                mapEngine(map, provider);
            }
        }
        
        
        this.contentTypeToTemplateEngineMap = map; //README probably some immutable
        
        logEngines();
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
            return provider.get();
        } else {
            log.warn("Did not find any engine for type: {}", contentType);
            return null;
        }
    }
    
    /**
     * Map the engine to all the content types it supports.
     * If any kind of overlap exists, a race condition occurs
     * @param map
     * @param engine
     */
    private void mapEngine(Map<String, Provider<? extends TemplateEngine>> map, Provider<? extends TemplateEngine> engine) {
        for (String type : engine.get().getContentType()) {
            map.put(type, engine);
        }
    }
    
    private void logEngines() {
        Set<String> types = getContentTypes();
        
        int maxContentTypeLen = 0;
        int maxParserEngineLen = 0;

        for (String contentType : types) {

            TemplateEngine engine = getTemplateEngineForContentType(contentType);

            maxContentTypeLen = Math.max(maxContentTypeLen,
                    contentType.length());
            maxParserEngineLen = Math.max(maxParserEngineLen,
                    engine.getClass().getName().length());

        }

        int borderLen = 6 + maxContentTypeLen + maxParserEngineLen;
        String border = StringUtil.padEnd("", borderLen, '-');

        log.info(border);
        log.info("Registered template engines");
        log.info(border);

        for (String contentType : types) {

            TemplateEngine engine = getTemplateEngineForContentType(contentType);
            log.info("{}  =>  {}",
                    StringUtil.padEnd(contentType, maxContentTypeLen, ' '),
                    engine.getClass().getName());

        }
    }

}
