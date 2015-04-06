package net.javapla.jawn.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlParser {
    
    private static final XmlMapper XML_MAPPER = new XmlMapper();
    static {
        // ensure same configuration for all parsers
        ParserConfiguration.config(XML_MAPPER);
    }
    
    public static final <T> T parseObject(Reader reader, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return XML_MAPPER.readValue(reader, clazz);
    }
    public static final <T> T parseObject(InputStream input, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return XML_MAPPER.readValue(input, clazz);
    }
    
    
    public static final void writeObject(Object obj, Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
        XML_MAPPER.writeValue(writer, obj);
    }

}
