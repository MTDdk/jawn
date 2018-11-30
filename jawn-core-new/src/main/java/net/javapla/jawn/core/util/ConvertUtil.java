package net.javapla.jawn.core.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;


public class ConvertUtil {

    
    /**
     * Returns string representation of an object.
     *
     * @param value value to convert.
     * @return string representation of an object.
     */
    public static String toString(Object value) {
        return value == null ? null : value.toString();
    }
    
    /**
     * Converts value to Integer if it can. If value is an Integer, it is returned, if it is a Number, it is
     * promoted to Integer and then returned, in all other cases, it converts the value to String,
     * then tries to parse Integer from it.
     *
     * @param value value to be converted to Integer.
     * @return value converted to Integer.
     * @throws ConversionException if failing to do the conversion
     */
    public static Integer toInteger(Object value) throws ConversionException {
        if (value == null) {
            return null;
        } else if (value instanceof Number) {
            return  ((Number)value).intValue();
        } else {
            NumberFormat nf = new DecimalFormat();
            try {
                return nf.parse(value.toString()).intValue();
            } catch (ParseException e) {
                throw new ConversionException("failed to convert: '" + value + "' to Integer", e);
            }
        }
    }
    /**
     * Same as {@linkplain ConvertUtil#toInteger(Object)} but does not throw an exception
     * if the conversion could not be done.
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static Integer toInteger(Object value, Integer defaultValue) {
        try {
            return toInteger(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Returns true if the value is any numeric type and has a value of 1, or
     * if string type has a value of 'y', 't', 'true' or 'yes'. Otherwise, return false.
     *
     * @param value value to convert
     * @return true if the value is any numeric type and has a value of 1, or
     * if string type has a value of 'y', 't', 'true' or 'yes'. Otherwise, return false.
     */
    public static Boolean toBoolean(Object value){
        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof BigDecimal) {
            return value.equals(BigDecimal.ONE);
        } else if (value instanceof Long) {
            return value.equals(1L);
        } else if (value instanceof Integer) {
            return value.equals(1);
        } else if (value instanceof Character) {
            return value.equals('y') || value.equals('Y')
                    || value.equals('t') || value.equals('T');

        }else return value.toString().equalsIgnoreCase("yes")
                || value.toString().equalsIgnoreCase("true") 
                || value.toString().equalsIgnoreCase("y")
                || value.toString().equalsIgnoreCase("t")
                || Boolean.parseBoolean(value.toString());
    }
    
    /**
     * Converts any value to <code>Double</code>.
     * @param value value to convert.
     * 
     * @return converted double. 
     * @throws ConversionException if the conversion failed
     */
    public static Double toDouble(Object value) throws ConversionException {
        if (value == null) {
            return null;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            NumberFormat nf = new DecimalFormat();
            try {
                return nf.parse(value.toString()).doubleValue();
            } catch (ParseException e) {
                throw new ConversionException("failed to convert: '" + value + "' to Double", e);
            }
        }
    }
    
    /**
     * Converts value to Long if c it can. If value is a Long, it is returned, if it is a Number, it is
     * promoted to Long and then returned, in all other cases, it converts the value to String,
     * then tries to parse Long from it.
     *
     * @param value value to be converted to Long.
     * @return value converted to Long.
     * @throws ConversionException if the conversion failed
     */
    public static Long toLong(Object value) throws ConversionException {
        if (value == null) {
            return null;
        } else if (value instanceof Number) {
            return  ((Number)value).longValue();
        } else {
            NumberFormat nf = new DecimalFormat();
            try {
                return nf.parse(value.toString()).longValue();
            } catch (ParseException e) {
                throw new ConversionException("failed to convert: '" + value + "' to Long", e);
            }
        }
    }
    
    public static class ConversionException extends RuntimeException {

        private static final long serialVersionUID = 5457757452289804224L;

        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConversionException(Throwable cause) {
            super(cause.getMessage(), cause);
        }
    }
    
}
