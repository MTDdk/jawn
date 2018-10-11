package net.javapla.jawn.core.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.exceptions.ParsableException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class JsonParserEngine implements ParserEngine {
    
    private final ObjectMapper mapper;
    
    @Inject
    public JsonParserEngine(ObjectMapper mapper) {
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
        try (InputStream s = stream ) {
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
        return MediaType.APPLICATION_JSON;
    }
    
//    public static final void writeObject(Object entity, Writer writer, String locale) throws JsonGenerationException, JsonMappingException, IOException {
//        JSON_MAPPER
////            .writer(new SimpleDateFormat("dd. MMMMM YYYY", new Locale(locale)))
//            .writeValue(writer, entity);
//    }
}
