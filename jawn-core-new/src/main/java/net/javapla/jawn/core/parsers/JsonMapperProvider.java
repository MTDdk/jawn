package net.javapla.jawn.core.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.inject.Provider;

/**
 * Creates and configure a Jackson JSON mapper
 * 
 * @author MTD
 */
public final class JsonMapperProvider implements Provider<ObjectMapper> {

    @Override
    public ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();
        
        ParserConfiguration.config(mapper);
        
        // for extra speed
        mapper.registerModule(new AfterburnerModule());
        
        return mapper;
    }

}
