package net.javapla.jawn.template.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Parser;
import net.javapla.jawn.core.Plugin;
import net.javapla.jawn.core.Renderer;

public class JacksonModule implements Plugin, Renderer, Parser {
    
    private final ObjectMapper mapper;
    private final ObjectWriter writer;
    
    public JacksonModule() {
        this.mapper = new ObjectMapper();
        this.writer = mapper.writer();
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
        //ctx.contentType(MediaType.JSON);
        //ctx.respond(writer.writeValueAsBytes(value));
        return null;
    }

}
