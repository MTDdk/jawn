package net.javapla.jawn.core.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;

public class JsonMapperProvider implements Provider<ObjectMapper> {

    @Override
    public ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();
        
        ParserConfiguration.config(mapper);
        
        return mapper;
    }

}
