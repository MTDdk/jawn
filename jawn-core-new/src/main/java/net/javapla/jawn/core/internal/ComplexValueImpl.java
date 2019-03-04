package net.javapla.jawn.core.internal;

import java.io.IOException;
import java.util.Collection;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.ParsableError;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.parsers.Parsable;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.parsers.ParserEngineManager;

class ComplexValueImpl /*extends ValueImpl implements Value.ComplexValue*/ {

//    private final ParserEngineManager engines;
//    private final MediaType type;
//
//    public ComplexValueImpl(final ParserEngineManager manager, final Parsable parsable) {
//        this(manager, MediaType.PLAIN, parsable);
//    }
//    
//    public ComplexValueImpl(final ParserEngineManager manager, final MediaType type, final Parsable parsable) {
//        super(parsable);
//        this.engines = manager;
//        this.type = type;
//    }
//    
//    @Override
//    public <T> T to(Class<T> type) throws Up.ParsableError {
//        return to(type, this.type);
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <T> T to(Class<T> type, MediaType mtype) throws Up.ParsableError {
//        T object = (T) results.get(type);
//        if (object == null) {
//            
//            ParserEngine engine = engines.getParserEngineForContentType(mtype);
//            if (engine == null) {
//                throw new Up.ParsableError("Failed to parse body as: " + mtype);
//            }
//            
//            try {
//                object = engine.invoke(parsable.bytes(), type);
//            } catch (IOException e) {
//                throw new Up.ParsableError("Failed to parse body as: " + mtype);
//            }
//            
//            results.put(type, object);
//        }
//        return object;
//    }
//    
//    @Override
//    public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType) throws Up.ParsableError {
//        return toCollection(type, collectionType, this.type);
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType, MediaType mtype) throws Up.ParsableError {
//        if (isCollection(collectionType)) {
//            C object = (C) results.get(type);
//            if (object == null) {
//
//                ParserEngine engine = engines.getParserEngineForContentType(mtype);
//                if (engine == null) {
//                    throw new Up.ParsableError("Failed to parse body as: " + mtype);
//                }
//
//                java.util.Collection<T> collection = (Collection<T>) collectionOfCollections.get(collectionType).get();
//
//                parsable.forEach(param -> {
//                    try {
//                        collection.add(engine.invoke(param.bytes(), type));
//                    } catch (ParsableError | IOException e) {
//                        throw new Up.ParsableError("Failed to parse body as: " + mtype);
//                    }
//                });
//                
//                object = (C) collection;
//                results.put(type, object);
//
//            }
//            return object;
//        }
//        
//        throw new Up.ParsableError("Collection type not supported: " + collectionType);
//    }
//    
//    static Value of(final ParserEngineManager manager, final Parsable parsable) {
//        return new ComplexValueImpl(manager, parsable);
//    }
//    
//    static Value of(final ParserEngineManager manager, final MediaType type, final Parsable parsable) {
//        return new ComplexValueImpl(manager, type, parsable);
//    }

}
