package net.javapla.jawn.parsers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.javapla.jawn.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ParserEngineManagerImpl implements ParserEngineManager {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    
    // Keep a reference of providers rather than instances, so body parser engines
    // don't have to be singleton if they don't want
    private final Map<String, Provider<? extends ParserEngine>> contentTypeToParserMap;
    
    @Inject
    public ParserEngineManagerImpl(Provider<JsonParserEngine> json,
                                   Provider<XmlParserEngine>  xml) {
        
        
        Map<String, Provider<? extends ParserEngine>> map = new HashMap<>();

        // First put the built in ones in, this is so they can be overridden by
        // custom bindings
        map.put(json.get().getContentType(), json);
        map.put(xml.get().getContentType(), xml);
        
        this.contentTypeToParserMap = map; //README some form of immutable
        
        logParsers();
    }
    
    @Override
    public Set<String> getContentTypes() {
        return contentTypeToParserMap.keySet(); //README make immutable
    }

    @Override
    public ParserEngine getParserEngineForContentType(String contentType) {
        Provider<? extends ParserEngine> provider = contentTypeToParserMap.get(contentType);
        
        if (provider != null) {
            return provider.get();
        }
        return null;
    }

    private void logParsers() {
        Set<String> types = getContentTypes();
        
        int maxContentTypeLen = 0;
        int maxParserEngineLen = 0;

        for (String contentType : types) {

            ParserEngine bodyParserEngine = getParserEngineForContentType(contentType);

            maxContentTypeLen = Math.max(maxContentTypeLen,
                    contentType.length());
            maxParserEngineLen = Math.max(maxParserEngineLen,
                    bodyParserEngine.getClass().getName().length());

        }

        int borderLen = 6 + maxContentTypeLen + maxParserEngineLen;
        String border = StringUtil.padEnd("", borderLen, '-');

        log.info(border);
        log.info("Registered request bodyparser engines");
        log.info(border);

        for (String contentType : types) {

            ParserEngine templateEngine = getParserEngineForContentType(contentType);
            log.info("{}  =>  {}",
                    StringUtil.padEnd(contentType, maxContentTypeLen, ' '),
                    templateEngine.getClass().getName());

        }
    }
}
