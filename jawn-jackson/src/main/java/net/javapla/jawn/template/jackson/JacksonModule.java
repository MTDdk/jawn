package net.javapla.jawn.template.jackson;

import java.io.InputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.javapla.jawn.core.Body;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Parser;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.Renderer;

public class JacksonModule implements Plugin, Renderer, Parser {
    
    private final ObjectMapper mapper;
    private final ObjectWriter writer;
    private final ObjectReader reader;
    private final TypeFactory typeFactory;
    
    public JacksonModule() {
        this.mapper = new ObjectMapper();
        this.writer = mapper.writer();
        this.reader = mapper.reader();
        this.typeFactory = mapper.getTypeFactory();
    }

    @Override
    public void install(Application config) {
        
        config.renderer(MediaType.JSON, this);
        config.parser(MediaType.JSON, this);
        
        
        config.registry().register(ObjectMapper.class, mapper);
    }

    @Override
    public byte[] render(Context ctx, Object value) throws Exception {
        ctx.resp().contentType(MediaType.JSON);
        ctx.resp().respond(writer.writeValueAsBytes(value));
        return null;
    }

    @Override
    public Object parse(Context ctx, Type type) throws Exception {
        Body body = ctx.req().body();
        if (!body.isPresent()) return null;
        if (body.inMemory()) {
            if (type == JsonNode.class) return reader.readTree(body.bytes());
            
            // TODO test which is faster
            //return reader.forType(typeFactory.constructType(type)).readValue(body.bytes());
            return mapper.readValue(body.bytes(), typeFactory.constructType(type));
        } else {
            try (InputStream stream = body.stream()) {
                if (type == JsonNode.class) return reader.readTree(stream);
                
                return mapper.readValue(stream, typeFactory.constructType(type));
            }
        }
    }
}
