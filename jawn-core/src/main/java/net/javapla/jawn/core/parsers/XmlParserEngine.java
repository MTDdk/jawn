package net.javapla.jawn.core.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.exceptions.ParsableException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;

class XmlParserEngine implements ParserEngine {
    
//    private static final XmlMapper XML_MAPPER = new XmlMapper();
//    static {
//        // ensure same configuration for all parsers
//        ParserConfiguration.config(XML_MAPPER);
//    }
//    
//    public static final <T> T parseObject(Reader reader, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
//        return XML_MAPPER.readValue(reader, clazz);
//    }
//    public static final <T> T parseObject(InputStream input, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
//        return XML_MAPPER.readValue(input, clazz);
//    }
//    
//    
//    public static final void writeObject(Object obj, Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
//        XML_MAPPER.writeValue(writer, obj);
//    }

    private final XmlMapper mapper;
    
    @Inject
    public XmlParserEngine(XmlMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public <T> T invoke(Reader reader, Class<T> clazz) throws ParsableException {
        try (Reader r = reader) {
            return mapper.readValue(r, clazz);
        } catch (IOException e) {
            throw new ParsableException(e);
        }
    }
    @Override
    public <T> T invoke(InputStream stream, Class<T> clazz) throws ParsableException {
        try (InputStream s = stream) {
            return mapper.readValue(s, clazz);
        } catch (IOException e) {
            throw new ParsableException(e);
        }
    }
    
    @Override
    public <T> T invoke(byte[] arr, Class<T> clazz) throws ParsableException {
        try {
            return mapper.readValue(arr, clazz);
        } catch (IOException e) {
            throw new ParsableException(e);
        }
    }
    
    @Override
    public String getContentType() {
        return MediaType.APPLICATION_XML;
    }

}
