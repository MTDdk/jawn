package net.javapla.jawn.core.parsers;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Provider;

public class XmlMapperProvider implements Provider<XmlMapper> {

    @Override
    public XmlMapper get() {
        XmlMapper mapper = new XmlMapper();
        
        ParserConfiguration.config(mapper);
        
        return mapper;
    }

}
