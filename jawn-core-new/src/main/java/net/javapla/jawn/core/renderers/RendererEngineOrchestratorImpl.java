package net.javapla.jawn.core.renderers;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Err;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.util.StringUtil;

@Singleton
public final class RendererEngineOrchestratorImpl implements RendererEngineOrchestrator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    
    // Keep a reference of providers rather than instances, so template engines
    // don't have to be singleton if they don't want
    private final Map<MediaType, Provider<? extends RendererEngine>> contentTypeToRendererEngineMap;
    //TODO
    // This map NEEDS to be of a different type, as the HashMap#get is fairly slow.
    // A suggested modification could be to introduce enums as Content Types for TemplateEngine#getContentType.
    // This would make the lookup of providers to be close to O(1).
    // Even better if the providers are sorted into some array of size Enum.values().length.
    // How to handle user defined content types has not yet been devised
    
    @Inject
    RendererEngineOrchestratorImpl(
             final Provider<JsonRendererEngine>           json,
             final Provider<XmlRendererEngine>            xml,
             final Provider<TextRendererEngine>           text,
             final Provider<StreamRendererEngine>         stream,
             //Provider<ImageRendererEngine>          image,
             //Provider<StringTemplateTemplateEngine> html,
             final Injector injector) {
        
        final Map<MediaType, Provider<? extends RendererEngine>> map = new HashMap<>();

        // Map built in content type bindings
        mapEngine(map, json);
        mapEngine(map, xml);
        mapEngine(map, text);
        mapEngine(map, stream);
        //mapEngine(map, image);
        // mapEngine(map, html);
        
        
        // Find any other defined bindings for TemplateEngine,
        // and put them into the map.
        // If a TemplateEngine handles the same content types
        // as already in the map, the existing will be overridden
        for (Entry<Key<?>, Binding<?>> binding : injector.getBindings().entrySet()) {
            if (RendererEngine.class.isAssignableFrom(binding.getKey().getTypeLiteral().getRawType())) {
                @SuppressWarnings("unchecked")
                Provider<? extends RendererEngine> provider = 
                    (Provider<? extends RendererEngine>) binding.getValue().getProvider();
                mapEngine(map, provider);
            }
        }
        
        this.contentTypeToRendererEngineMap = Collections.unmodifiableMap(map);
        
        logEngines();
    }
    
    @Override
    public Set<MediaType> getContentTypes() {
        return contentTypeToRendererEngineMap.keySet();
    }

    @Override
    public final void getRendererEngineForContentType(final MediaType contentType, final Consumer<RendererEngine> callback) throws Err.BadMediaType {
        final Provider<? extends RendererEngine> provider = contentTypeToRendererEngineMap.get(contentType);
        
        if (provider != null) {
            callback.accept(provider.get());
        } else {
            if (contentType.matches(MediaType.HTML)) {
                log.warn("You might want to include jawn-templates-stringtemplate or another template engine in your classpath");
            }
            throw new Err.BadMediaType(
                MessageFormat.format("Could not find a template engine supporting the content type of the response : {0}", contentType));
        }
    }
    
    /**
     * Map the engine to all the content types it supports.
     * If any kind of overlap exists, a race condition occurs
     * @param map
     * @param engine
     */
    private void mapEngine(Map<MediaType, Provider<? extends RendererEngine>> map, Provider<? extends RendererEngine> engine) {
        for (MediaType type : engine.get().getContentType()) {
            map.put(type, engine);
        }
    }
    
    private void logEngines() {
        Set<MediaType> types = getContentTypes();

        int maxContentTypeLen = 0;
        int maxParserEngineLen = 0;

        for (MediaType contentType : types) {
            
            RendererEngine engine = contentTypeToRendererEngineMap.get(contentType).get();
            
            maxContentTypeLen = Math.max(maxContentTypeLen, contentType.name().length());
            maxParserEngineLen = Math.max(maxParserEngineLen, engine.getClass().getName().length());
        }

        int borderLen = 6 + maxContentTypeLen + maxParserEngineLen;
        String border = StringUtil.padEnd("", borderLen, '-');

        log.info(border);
        log.info("Registered template engines");
        log.info(border);

        for (MediaType contentType : types) {
            RendererEngine engine = contentTypeToRendererEngineMap.get(contentType).get();
            log.info("{}  =>  {}", StringUtil.padEnd(contentType.name(), maxContentTypeLen, ' '), engine.getClass().getName());
        }
    }

}
