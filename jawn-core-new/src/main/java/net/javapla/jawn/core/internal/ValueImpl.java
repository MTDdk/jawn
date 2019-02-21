package net.javapla.jawn.core.internal;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Supplier;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.ParsableError;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.parsers.Parsable;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.parsers.ParserEngineManager;

class ValueImpl implements Value {
    
    private final HashMap<Object, Object> results = new HashMap<>(1);
    
    private final ParserEngineManager engines;
    private final MediaType type;
    private final Parsable parsable;

    public ValueImpl(final ParserEngineManager manager, final Parsable parsable) {
        this(manager, MediaType.PLAIN, parsable);
    }
    
    public ValueImpl(final ParserEngineManager manager, final MediaType type, final Parsable parsable) {
        this.engines = manager;
        this.type = type;
        this.parsable = parsable;
    }
    
    @Override
    public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType) throws ParsableError {
        return toCollection(type, collectionType, this.type);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType, MediaType mtype) throws ParsableError {
        if (isCollection(collectionType)) {
            C object = (C) results.get(type);
            if (object == null) {

                ParserEngine engine = engines.getParserEngineForContentType(mtype);
                if (engine == null) {
                    throw new Up.ParsableError("Failed to parse body as: " + mtype);
                }

                java.util.Collection<T> collection = (Collection<T>) collectionOfCollections.get(collectionType).get();

                parsable.forEach(param -> {
                    collection.add(engine.invoke(param, type));
                });
                
                object = (C) collection;
                results.put(type, object);

            }
            return object;
        }
        
        return null;
    }
    
    @Override
    public <T> T to(Class<T> type) throws ParsableError {
        return to(type, this.type);
    }
    
    @Override
    public <T> T to(Class<T> type, MediaType mtype) throws ParsableError {
        @SuppressWarnings("unchecked")
        T object = (T) results.get(type);
        if (object == null) {
            
            ParserEngine engine = engines.getParserEngineForContentType(mtype);
            if (engine == null) {
                throw new Up.ParsableError("Failed to parse body as: " + mtype);
            }
            
            object = engine.invoke(parsable, type);
            
            results.put(type, object);
            
        }
        return object;
    }
    
    @Override
    public boolean isPresent() {
        return parsable.length() > 0;
    }
    
    private Map<Type, Supplier<java.util.Collection<?>>> collectionOfCollections = 
        Map.of(
            List.class, java.util.ArrayList::new,
            Set.class, java.util.HashSet::new,
            SortedSet.class, java.util.TreeSet::new
        );
    private boolean isCollection(Type type) {
        return collectionOfCollections.containsKey(type);
    }
    
    
    static Value of(final ParserEngineManager manager, final Parsable parsable) {
        return new ValueImpl(manager, parsable);
    }
    
    static Value of(final ParserEngineManager manager, final MediaType type, final Parsable parsable) {
        return new ValueImpl(manager, type, parsable);
    }

    static Value empty() {
        return new Empty();
    }
    

    static class Empty implements Value {

        @Override
        public <T> T to(Class<T> type) {
            throw new Up(Status.BAD_REQUEST);
        }

        @Override
        public <T> T to(Class<T> type, MediaType mtype) throws ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }

        @Override
        public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType) throws ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }

        @Override
        public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType, MediaType mtype) throws ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }
        
        @Override
        public boolean isPresent() {
            return false;
        }
    }
    
    
}
