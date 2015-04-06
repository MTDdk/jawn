package net.javapla.jawn.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    static {
        // ensure same configuration for all parsers
        ParserConfiguration.config(JSON_MAPPER);
    }
    
    public static final <T> T parseObject(Reader reader, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return JSON_MAPPER.readValue(reader, clazz);
    }
    
    public static final <T> T parseObject(InputStream input, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        return JSON_MAPPER.readValue(input, clazz);
    }
    
    
    public static final void writeObject(Object entity, Writer writer) throws JsonGenerationException, JsonMappingException, IOException {
        JSON_MAPPER.writeValue(writer, entity);
    }
    
//    public static final void writeObject(Object entity, Writer writer, String locale) throws JsonGenerationException, JsonMappingException, IOException {
//        JSON_MAPPER
////            .writer(new SimpleDateFormat("dd. MMMMM YYYY", new Locale(locale)))
//            .writeValue(writer, entity);
//    }
}
