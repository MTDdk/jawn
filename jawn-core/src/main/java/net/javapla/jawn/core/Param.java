package net.javapla.jawn.core;

import static net.javapla.jawn.core.util.ConvertUtil.toBoolean;
import static net.javapla.jawn.core.util.ConvertUtil.toDouble;
import static net.javapla.jawn.core.util.ConvertUtil.toInteger;
import static net.javapla.jawn.core.util.ConvertUtil.toLong;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.javapla.jawn.core.exceptions.ParsableException;
import net.javapla.jawn.core.util.ConversionException;

/**
 * Represents a parameter sent to the framework.
 * Works as a helper class for converting into various data types
 * 
 * @author MTD
 */
public class Param {
    
    final String param;

    Param(String param) {
        this.param = param;
    }
    
    public boolean isPresent() {
        return param != null;
    }
    
    public Integer asInt() {
        return toInteger(param);
    }
    public Integer asInt(int defaultValue) {
        try {
            Integer integer = toInteger(param);
            if (integer == null) return defaultValue;
            return integer;
        } catch (ConversionException e) {
            return defaultValue;
        }
    }
    public Boolean asBoolean() {
        return toBoolean(param);
    }
    public Long asLong() {
        return toLong(param);
    }
    public Double asDouble() {
        return toDouble(param);
    }
    public String asString() {
        return param;
    }
    /**
     * Convert the parameter into an {@link URL} representation
     * 
     * @return 
     *      The formed URL
     * @throws ParsableException 
     *      If the parameter was not on a correct format
     */
    public URL asURL() throws ParsableException {
        try {
            return new URL(param);
        } catch (MalformedURLException e) {
            throw new ParsableException(e);
        }
    }
    
    /**
     * Convert the parameter into a {@link Date} based on the provided <code>format</code>
     * @param format 
     *      Like "dd-MM-yy"
     * @return 
     *      Parsed <code>Date</code>
     * @throws ParsableException 
     *      If the parameter was not on the same form as <code>format</code>
     */
    public Date asDate(String format) throws ParsableException {
        SimpleDateFormat dateformat = new SimpleDateFormat(format);
        return asDate(dateformat);
    }
    /**
     * Convert the parameter into a {@link Date} based on the provided <code>format</code>
     * @param format 
     *      Could be a <code>new SimpleDateFormat("dd-MM-yy")</code>
     * @return 
     *      Parsed <code>Date</code>
     * @throws ParsableException 
     *      If the parameter was not on the same form as <code>format</code>
     */
    public Date asDate(DateFormat format) throws ParsableException {
        try {
            return format.parse(param);
        } catch (ParseException e) {
            throw new ParsableException(e);
        }
    }
    
    /**
     * Not implemented
     */
    /*public Map<String, String> fromJson() {
    }*/
    
    
    @Override
    public String toString() {
        return param;
    }
}
