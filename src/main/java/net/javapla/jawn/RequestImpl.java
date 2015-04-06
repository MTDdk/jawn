package net.javapla.jawn;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import net.javapla.jawn.exceptions.MediaTypeException;
import net.javapla.jawn.exceptions.ParsableException;
import net.javapla.jawn.parsers.JsonParser;

/**
 * Represents a request from the client and can be used to convert the request into useful representations
 * 
 * @author MTD
 */
class RequestImpl implements Request.Impl {
    private final HttpServletRequest request;
    RequestImpl(HttpServletRequest request) {
        this.request = request;
    }
    
    /**
     * Converts the request input into an object of the specified class in case of <code>application/json</code> request.
     *  
     * @param clazz A representation of the expected JSON
     * @return The object of the converted JSON, or <code>throws</code> if the JSON could not be correctly deserialized,
     *         or the media type was incorrect. 
     * @throws ParsableException If the parsing from JSON to class failed
     * @throws MediaTypeException If the mediatype of the request was not "application/json"
     * @author MTD
     */
    @Override
    public <T> T fromJson(Class<T> clazz) throws ParsableException, MediaTypeException {
        String contentType = request.getContentType();
        
        if (!MediaType.APPLICATION_JSON.equals(contentType))
            throw new MediaTypeException("Media type was not: " + MediaType.APPLICATION_JSON);
        
        try (InputStream stream = request.getInputStream()) {
            return JsonParser.parseObject(stream, clazz);
        } catch (IOException e) {
            throw new ParsableException(clazz);
        }
    }
    
    /**
     * Conveniently converts any input in the request into a string
     *
     * @return data sent by client as string.
     * @throws ParsableException If the request could not be correctly read as a stream
     */
    @Override
    public String asText() throws ParsableException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            return reader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            throw new ParsableException("Reading the input failed");
        }
    }
    
    /**
     * Get the inputstream of the request
     * @return the inputstream of the request
     * @throws IOException if an input or output error occurs
     */
    @Override
    public InputStream asStream() throws IOException {
        return request.getInputStream();
    }
    
    /**
     * Reads entire request data as byte array. Do not use for large data sets to avoid
     * memory issues.
     *
     * @return data sent by client as string.
     * @throws IOException
     */
    @Override
    public byte[] asBytes() throws IOException {
        try (ServletInputStream stream = request.getInputStream()) {
            ByteArrayOutputStream array = new ByteArrayOutputStream(stream.available());
            return array.toByteArray();
        }
    }
    
/* ****************
 *  Internal
 ***************** */
    
}