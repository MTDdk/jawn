package net.javapla.jawn.core;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.javapla.jawn.core.internal.ValueParser;
import net.javapla.jawn.core.util.DateUtil;
import net.javapla.jawn.core.util.StringUtil;

public interface Value extends Iterable<Value> { // SimpleValue
    
    /**
     * {@link Boolean#parseBoolean(String)} always returns either <code>true</code> or <code>false</code>
     * regardless of input
     * 
     * <i>(That is why no <code>booleanValue(default)</code> exists)</i>
     * 
     * @see Boolean#parseBoolean
     * @return
     */
    default boolean booleanValue() {
        return Boolean.parseBoolean(value());
    }
    
    default double doubleValue() {
        return simpleValue(Double::parseDouble, double.class);
    }
    
    default double doubleValue(final double fallback) {
        try {
            return doubleValue();
        } catch (Up.ParsableError e) {
            return fallback;
        }
        //return toOptional().map(Double::parseDouble).orElse(fallback);//toOptional(Double.class).orElse(fallback);
    }
    
    default int intValue() {
        return simpleValue(Integer::parseInt, int.class);
    }
    
    default int intValue(final int fallback) {
        //return map((s) -> (Integer::parseInt)).orElse(fallback);
        try {
            return intValue();
        } catch (Up.ParsableError e) {
            return fallback;
        }
    }
    
    default long longValue() {
        try {
            return Long.valueOf(value());
        } catch (NumberFormatException e) {
            // might be a date
            try {
                return DateUtil.fromDateString(value()).toEpochMilli();
            } catch (DateTimeParseException ignored) { }
            throw new Up.ParsableError("Failed to parse value as: " + long.class, e);
        }
    }
    
    default long longValue(final long fallback) {
        try {
            return longValue();
        } catch (Up.ParsableError e) {
            return fallback;
        }
        //return toOptional().map(Long::parseLong).orElse(fallback);
    }
    
    private <T extends Number> T simpleValue(final Function<String, T> callback, Class<T> type) {
        try {
            return callback.apply(value());
        } catch (NumberFormatException e) {
            throw new Up.ParsableError("Failed to parse value as: " + type, e);
        }
    }
    
    
    @Override
    default Iterator<Value> iterator() {
        return Collections.singleton(this).iterator();
    }
    
    default String value(final String fallback) {
        return toOptional().orElse(fallback);
    }
    
    default <T extends Enum<T>> T toEnum(final Class<T> type) {
        EnumSet<T> set = EnumSet.allOf(type);
        return set
            .stream()
            .filter(e -> e.name().equalsIgnoreCase(value()))
            .findFirst()
            .orElseGet(() -> java.lang.Enum.valueOf(type, value()));
    }
    
    default <T> T to(Class<T> type) {
        return ValueParser.to(this, type);
    }
    
    // Commented out until we know if we ever are going to need it
    /*default Object to(Type type) {
        return ValueParser.to(this, type);
    }*/
    
    default List<String> toList() {
        if (isPresent())
            return Collections.singletonList(value());
        return Collections.emptyList();
    }
    
    
    default <T> List<T> toList(final Class<T> type) {
        return ValueParser.toCollection(this, type, new ArrayList<T>());
    }
    
    default Set<String> toSet() {
        return Collections.singleton(value());
    }
    
    default <T> Set<T> toSet(final Class<T> type) {
        return ValueParser.toCollection(this, type, new HashSet<T>());
    }
    
    default Optional<String> toOptional() {
        if (!isPresent()) return Optional.empty();
        try {
            return Optional.of(value());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    default <T> Optional<T> toOptional(final Class<T> type) {
        //if (type.isPrimitive()) throw new IllegalArgumentException("Primitive types are not allowed in type parameters: " + type);
        if (!isPresent()) return Optional.empty();
        try {
            return Optional.ofNullable(ValueParser.to(this, type));//to(type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    String value();

    boolean isPresent();
    
    default void ifPresent(final Consumer<String> action) {
        if (isPresent()) {
            action.accept(value());
        }
    }
    
    default <T> void ifPresent(final Class<T> type, Consumer<T> action) {
        if (isPresent()) {
            action.accept(to(type));
        }
    }
    
    default <U> Optional<U> map(Function<String, U> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(value()));
        }
    }
    
    default <T,U> Optional<U> map(Class<T> type, Function<T, ? extends U> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(ValueParser.to(this, type)));
        }
    }
    
    /*default <U> Optional<U> mapBoolean(Function<Boolean, U> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(booleanValue()));
        }
    }
    
    default <U> Optional<U> mapDouble(Function<Double, U> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(doubleValue()));
        }
    }
    
    default <U> Optional<U> mapInteger(Function<Integer, U> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(intValue()));
        }
    }
    
    default <U> Optional<U> mapLong(Function<Long, U> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(longValue()));
        }
    }*/
    
    static Value of(final String value) {
        return new StringValue(value);
    }
    
    static Value of(final String ... valuables) {
        return of(Arrays.asList(valuables));//new ListValue(Arrays.asList(valuables).stream().map(StringValue::new).collect(Collectors.toList()));
    }
    
    static Value of(final Collection<String> valuables) {
        return new ListValue(valuables.stream().map(StringValue::new).collect(Collectors.toList()));
    }
    
    static Value of(final Optional<String> opt) {
        return opt.map(StringValue::new).orElse(new StringValue());
    }
    
    static Value of(final Value ... valuables) {
        return new ListValue(Arrays.asList(valuables));
    }
    
    static Value of(final List<Value> valuables) {
        return new ListValue(valuables);
    }
    
    
    class StringValue implements Value {
        private final String value;
        
        public StringValue() {
            this(null);
        }
        
        public StringValue(final String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
        
        @Override
        public boolean isPresent() {
            return !StringUtil.blank(value);
        }
        
        @Override
        public String toString() {
            return value != null
                ? String.format("StringValue[%s]", value)
                : "Value.empty";
        }
    }
    
    class ListValue implements Value {
        
        private final List<Value> valuables;

        public ListValue(final List<Value> valuables) {
            this.valuables = valuables;
        }

        @Override
        public String value() {
            return valuables.get(0).value();
        }

        @Override
        public List<String> toList() {
            return valuables.stream().map(Value::value).collect(Collectors.toList());
        }

        @Override
        public Set<String> toSet() {
            return valuables.stream().map(Value::value).collect(Collectors.toSet());
        }

        @Override
        public boolean isPresent() {
            return !valuables.isEmpty();
        }
        
        @Override
        public Iterator<Value> iterator() {
            return valuables.iterator();
        }
        
    }
    
    static Value empty() {
        return new Value() {

            @Override
            public String value() {
                return null;
            }

            @Override
            public boolean isPresent() {
                return false;
            }
            
        };
    }
}
