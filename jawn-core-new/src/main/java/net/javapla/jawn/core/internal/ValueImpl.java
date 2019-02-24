package net.javapla.jawn.core.internal;

import static net.javapla.jawn.core.util.StringUtil.NOT_BLANK;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Supplier;

import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.parsers.Parsable;
import net.javapla.jawn.core.util.DateUtil;

class ValueImpl implements Value {
    
    private interface BasicParser {
        <T> T parse(Parsable parsable, Class<T> type) throws Throwable;
        <T> boolean matches(Class<T> type);
    }
    
    private enum Parsers implements BasicParser {
        Simple {
            private final Map<Class<?>, Function<String, Object>> parsers = 
                Map.of(
                    String.class,  v -> v,
                    int.class,     NOT_BLANK.andThen(Integer::valueOf),
                    Integer.class, NOT_BLANK.andThen(Integer::valueOf),
                    long.class,    NOT_BLANK.andThen(this::toLong),
                    Long.class,    NOT_BLANK.andThen(this::toLong),
                    double.class,  NOT_BLANK.andThen(Double::valueOf),
                    Double.class,  NOT_BLANK.andThen(Double::valueOf)
                );
            
            @SuppressWarnings("unchecked")
            @Override
            public <T> T parse(Parsable parsable, Class<T> type) throws Throwable {
                Function<String, Object> function = parsers.get(type);
                return (T) function.apply(parsable.text());
            }
            
            private Long toLong(final String value) {
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException e) {
                    // might be a date
                    try {
                        return DateUtil.fromDateString(value).toEpochMilli();
                    } catch (DateTimeParseException ignored) {
                        throw e;
                    }
                }
            }
            
            @Override
            public <T> boolean matches(Class<T> type) {
                return parsers.containsKey(type);
            }
        },
        Bytes {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T parse(Parsable parsable, Class<T> type) throws Throwable {
                return (T) parsable.bytes();
            }

            @Override
            public <T> boolean matches(Class<T> type) {
                return type == byte[].class;
            }
            
        }, Enum {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public <T> T parse(Parsable parsable, Class<T> type) throws Throwable {
                Set<Enum> set = EnumSet.allOf((Class<? extends Enum>) type);
                final String value = parsable.text();
                return (T) set.stream()
                    .filter(e -> e.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElseGet(() -> java.lang.Enum.valueOf((Class<? extends Enum>) type, value));
            }

            @Override
            public <T> boolean matches(Class<T> type) {
                return Enum.class.isAssignableFrom(type);
            }
        }
    }
    
    protected final HashMap<Object, Object> results = new HashMap<>(1);
    
    //private final ParserEngineManager engines;
    //private final MediaType type;
    protected final Parsable parsable;

    /*public ValueImpl(final ParserEngineManager manager, final Parsable parsable) {
        this(manager, MediaType.PLAIN, parsable);
    }*/
    
    public ValueImpl(/*final ParserEngineManager manager, final MediaType type, */final Parsable parsable) {
        //this.engines = manager;
        //this.type = type;
        this.parsable = parsable;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType) throws Up.ParsableError {
        if (isCollection(collectionType)) {
            C object = (C) results.get(type);
            if (object == null) {

                BasicParser parser = parser(type);

                java.util.Collection<T> collection = (Collection<T>) collectionOfCollections.get(collectionType).get();

                    parsable.forEach(param -> {
                        try {
                            collection.add(parser.parse(param, type));
                        } catch (Throwable e) {
                            throw new Up.ParsableError("Failed to parse value as: " + type);
                        }
                    });
                
                object = (C) collection;
                results.put(type, object);

            }
            return object;
        }
        
        throw new Up.ParsableError("Collection type not supported: " + collectionType);
    }
    
    @Override
    public <T> T to(Class<T> type) throws Up.ParsableError {
        @SuppressWarnings("unchecked")
        T object = (T) results.get(type);
        if (object == null) {
            
            try {
                object = parser(type).parse(parsable, type);
            } catch (Throwable e) {
                throw new Up.ParsableError("Failed to parse value as: " + type);
            }
            
            results.put(type, object);
            
        }
        return object;
    }
    
    protected <T> BasicParser parser(Class<T> type) {
        
        if (Parsers.Simple.matches(type)) {
            return Parsers.Simple;//.parse(value, type);
            
        } else if (Parsers.Bytes.matches(type)) {
            return Parsers.Bytes;//.parse(value, type);
            
        } else if (Parsers.Enum.matches(type)) {
            return Parsers.Enum;//.parse(value, type);
        }
        
        
        throw new Up.ParsableError("No parser for type " + type);
    }
    
    @Override
    public boolean isPresent() {
        return parsable.length() > 0;
    }
    
    @Override
    public String toString() {
        try {
            return parsable.text();
        } catch (IOException e) {
            return parsable.toString();
        }
    }
    
    protected Map<Type, Supplier<java.util.Collection<?>>> collectionOfCollections = 
        Map.of(
            List.class, java.util.ArrayList::new,
            Set.class, java.util.HashSet::new,
            SortedSet.class, java.util.TreeSet::new
        );
    protected boolean isCollection(Type type) {
        return collectionOfCollections.containsKey(type);
    }
    
    
    /*static Value of(final ParserEngineManager manager, final Parsable parsable) {
        return new ValueImpl(manager, parsable);
    }
    
    static Value of(final ParserEngineManager manager, final MediaType type, final Parsable parsable) {
        return new ValueImpl(manager, type, parsable);
    }*/
    static Value of(final Parsable parsable) {
        return new ValueImpl(parsable);
    }

    static Value empty() {
        return new Empty();
    }
    

    static class Empty implements Value {

        @Override
        public <T> T to(Class<T> type) {
            throw new Up(Status.BAD_REQUEST);
        }

        /*@Override
        public <T> T to(Class<T> type, MediaType mtype) throws ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }*/

        @Override
        public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType) throws Up.ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }

        /*@Override
        public <C extends Collection<T>, T> C toCollection(Class<T> type, Class<C> collectionType, MediaType mtype) throws ParsableError {
            throw new Up(Status.BAD_REQUEST);
        }*/
        
        @Override
        public boolean isPresent() {
            return false;
        }
    }
    
    
}
