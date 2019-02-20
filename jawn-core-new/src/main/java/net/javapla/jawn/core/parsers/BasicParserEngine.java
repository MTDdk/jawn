package net.javapla.jawn.core.parsers;

import static net.javapla.jawn.core.util.StringUtil.NOT_BLANK;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.inject.Singleton;

import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.ParsableError;
import net.javapla.jawn.core.util.DateUtil;

@Singleton
public class BasicParserEngine implements ParserEngine {
    
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
            public <T> T parse(Parsable parsable, Class<T> type) throws ParsableError {
                try {
                    Function<String, Object> function = parsers.get(type);
                    //return (T) function.apply(parsable.text());
                    return (T) function.apply(new String(parsable.bytes()));
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
            public <T> boolean matches(Class<T> type) {
                return parsers.containsKey(type);
            }
        },
        Bytes {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T parse(Parsable parsable, Class<T> type) throws ParsableError {
                try {
                    return (T) parsable.bytes();
                } catch (IOException e) {
                    throw new Up.ParsableError(e);
                }
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
                String value = new String(parsable.bytes());
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
    
    @Override
    public <T> T invoke(Parsable parsable, Class<T> type) throws ParsableError {
        
        try {
            if (Parsers.Simple.matches(type)) {
                return Parsers.Simple.parse(parsable, type);
                
            } else if (Parsers.Bytes.matches(type)) {
                return Parsers.Bytes.parse(parsable, type);
                
            } else if (Parsers.Enum.matches(type)) {
                return Parsers.Enum.parse(parsable, type);
            }
        } catch (Throwable e) {
            throw new Up.ParsableError(e);
        }
        
        throw new Up.ParsableError("No parser for type " + type);
        
    }
    
    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.PLAIN, MediaType.TEXT };
    }
}
