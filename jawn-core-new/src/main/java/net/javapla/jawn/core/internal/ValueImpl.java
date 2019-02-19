package net.javapla.jawn.core.internal;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.parsers.ParserEngine.Parsable;
import net.javapla.jawn.core.parsers.ParserEngineManager;

class ValueImpl implements Value {
    
    private final HashMap<Object, Object> results = new HashMap<>(1);
    
    private final ParserEngineManager engines;
    private final MediaType type;
    private final Parsable parsable;

    public ValueImpl(final ParserEngineManager manager, final ParserEngine.Parsable parsable) {
        this(manager, MediaType.PLAIN, parsable);
    }
    
    public ValueImpl(final ParserEngineManager manager, final MediaType type, final ParserEngine.Parsable parsable) {
        this.engines = manager;
        this.type = type;
        this.parsable = parsable;
    }

    @Override
    public <T> T to(ParameterizedType/*Class<T>*/ type) throws Up.ParsableError {
        return to(type, this.type);
    }

    @Override
    public <T> T to(ParameterizedType type, MediaType mtype) throws Up.ParsableError {
        @SuppressWarnings("unchecked")
        T object = (T) results.get(type);
        if (object == null) {
            
            ParserEngine engine = engines.getParserEngineForContentType(mtype);
            if (engine == null) {
                throw new Up.ParsableError("Failed to parse body as " + mtype);
            }
            
            object = engine.invoke(parsable, type);
            
            results.put(type, object);
        }
        return object;
    }

    static class Empty implements Value {

        @Override
        public <T> T to(ParameterizedType type) throws Up.ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }

        @Override
        public <T> T to(ParameterizedType type, MediaType mtype) throws Up.ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }
        
    }
}
