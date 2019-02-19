package net.javapla.jawn.core.parsers;

import static net.javapla.jawn.core.util.StringUtil.NOT_BLANK;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.inject.Singleton;
import com.google.inject.util.Types;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.ParsableError;
import net.javapla.jawn.core.util.DateUtil;

@Singleton
public class BasicParserEngine implements ParserEngine {
    
    private interface BasicParser {
        Object parse(ParserEngine.Parsable parsable, Type type) throws Up.ParsableError;
        boolean matches(Type type);
    }
    
    private enum Parsers implements BasicParser {
        Simple {
            private final Map<Class<?>, Function<String, Object>> parsers = Map.of(
                String.class, v -> v,
                int.class, NOT_BLANK.andThen(Integer::valueOf),
                Integer.class, NOT_BLANK.andThen(Integer::valueOf),
                long.class, NOT_BLANK.andThen(this::toLong),
                Long.class, NOT_BLANK.andThen(this::toLong)
            );
            
            @Override
            public Object parse(Parsable parsable, Type type) throws Up.ParsableError {
                try {
                    Function<String, Object> function = parsers.get(type);
                    return function.apply(parsable.text());
                } catch (IOException e) {
                    throw new Up.ParsableError(e);
                }
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
            public boolean matches(Type type) {
                return parsers.containsKey(type);
            }
        },
        Optional {
            
            @Override
            public Object parse(Parsable parsable, Type type) {
                if (parsable.length() == 0) return java.util.Optional.empty();
                try {
                    return java.util.Optional.ofNullable(Simple.parse(parsable, type));
                }catch (Up.ParsableError e) {
                    return java.util.Optional.empty();
                }
            }
            
            @Override
            public boolean matches(Type type) {
                return type == Optional.class;
            }
        },
        Bytes {

            @Override
            public Object parse(Parsable parsable, Type type) throws ParsableError {
                try {
                    return parsable.bytes();
                } catch (IOException e) {
                    throw new Up.ParsableError(e);
                }
            }

            @Override
            public boolean matches(Type type) {
                return type == byte[].class;
            }
            
        },
        Collection {

            private Map<Type, Supplier<java.util.Collection<?>>> collectionOfCollections = 
                Map.of(
                    List.class, java.util.ArrayList::new,
                    Set.class, java.util.HashSet::new,
                    SortedSet.class, java.util.TreeSet::new
                );
            
            @Override
            public Object parse(Parsable parsable, Type type) throws Up.ParsableError {
                //ParameterizedType pt = (ParameterizedType) type;
                
                //java.util.Collection<?> collection = collectionOfCollections.get(pt.getRawType()).get();
                
                //return collection;
                throw new Up(500, "not implemented");
            }

            @Override
            public boolean matches(Type type) {
                return collectionOfCollections.containsKey(type);
            }
            
        }
        
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(final Parsable parsable, final Class<T> clazz) throws Up.ParsableError {
        return (T) invoke(parsable, Types.newParameterizedType(clazz));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final Parsable parsable, final ParameterizedType type) throws Up.ParsableError {
        
        Type type1 = type.getRawType();
        if (Parsers.Simple.matches(type1)) {
            return Parsers.Simple.parse(parsable, type1);
        } else if (Parsers.Collection.matches(type1)) {
            return Parsers.Collection.parse(parsable, type);
        } else if (Parsers.Optional.matches(type1)) {
            return Parsers.Optional.parse(parsable, type.getActualTypeArguments()[0]);
        } else if (Parsers.Bytes.matches(type1)) {
            return Parsers.Bytes.parse(parsable, type1);
        }
        
        throw new Up.ParsableError("No parser for type " + type);
    }
    
    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.PLAIN, MediaType.TEXT };
    }
}
