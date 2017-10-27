package net.javapla.jawn.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import net.javapla.jawn.core.util.MultiList;

public interface Request {
    
    HttpMethod method();
    String path();
    String contextPath();
    String queryString();
    Optional<String> param(String name);
    MultiList<String> params();
    List<String> params(String name) throws Exception;
    List<String> headers(String name);
    Optional<String> header(final String name);
    List<String> headerNames();
    List<Cookie> cookies();
    //something handling upload
    List<FormItem> files();//throws Exception
    
    
    // ****************
    // Request manipulation
    // ****************
    /**
     * Get the inputstream of the request
     * @return the inputstream of the request
     * @throws IOException if an input or output error occurs
     */
    InputStream in() throws IOException;
    /**
     * Reads entire request data as byte array. Do not use for large data sets to avoid
     * memory issues.
     *
     * @return data sent by client as string.
     * @throws IOException
     */
    byte[] bytes() throws IOException;
    
    
    String ip();
    String protocol();
    
    /**
     * Host port
     * @return The port part of the destination request
     */
    int port();
    String scheme();
    //boolean secure();
    /**
     * Upgrade the request to something else...like a web socket.
     *
     * @param type Upgrade type.
     * @param <T> Upgrade type.
     * @return A instance of the upgrade.
     * @throws Exception If the upgrade fails or it is un-supported.
     * @see NativeWebSocket
     */
    //<T> T upgrade(Class<T> type) throws Exception;
    void startAsync();
    
    
    /**
     * This will try to parse the request body nicely into an object.
     * It determines the parser based on the request type.
     * 
     * You can register your own parsers for other request types, if needed.
     * Take a look at: {@link ParserEngine} and {@link ParserEngineManager}
     * 
     * @param clazz A representation of the expected body
     * @return The parsed request object, or <code>throws</code> if the body could not be correctly deserialized,
     *         or the media type was incorrect.
     * @throws ParsableException If the parsing from the given content type to class failed
     * @throws MediaTypeException If the media type of the request was not specified
     */
    //<T> T parseBody(Class<T> clazz) throws ParsableException, MediaTypeException;
}
