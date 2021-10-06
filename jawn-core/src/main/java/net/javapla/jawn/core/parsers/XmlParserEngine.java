package net.javapla.jawn.core.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.ParsableError;

@Singleton
final class XmlParserEngine implements ParserEngine {
    
    private final XmlMapper mapper;
    private final TypeFactory typeFactory;
    
    @Inject
    XmlParserEngine(XmlMapper mapper) {
        this.mapper = mapper;
        typeFactory = mapper.getTypeFactory();
    }
    
    @Override
    public Object invoke(Context context, Type type) throws ParsableError {
        if ( type == JsonNode.class) {
            try {
                return mapper.readTree(context.req().in());
            } catch (IOException e) {
                throw Up.ParsableError.here(e);
            }
        }
        try (InputStream in = context.req().in()) {
            return mapper.readValue(in, typeFactory.constructType(type));
        } catch (IOException e) {
            throw Up.ParsableError.here(e);
        }
        //context.req().bytes();
    }
    
    
    
    /*@Override
    public <T> T invoke(InputStream stream, Class<T> clazz) throws Up.ParsableError {
        try (InputStream s = stream) {
            return mapper.readValue(s, clazz);
        } catch (IOException e) {
            throw new Up.ParsableError(e);
        }
    }
    
    @Override
    public <T> T invoke(byte[] arr, Class<T> clazz) throws Up.ParsableError {
        try {
            return mapper.readValue(arr, clazz);
        } catch (IOException e) {
            throw new Up.ParsableError(e);
        }
    }*/
    
    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.XML };
    }

}
