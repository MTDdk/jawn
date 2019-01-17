package net.javapla.jawn.core.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import net.javapla.jawn.core.Cookie;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.util.MultiList;

public interface ServerRequest {
    
    HttpMethod method();
    String path();
    //String contextPath();
    String queryString();
    
    /**
     * Only query parameters - not any form data
     * @return
     */
    MultiList<String> queryParams();
    default List<String> queryParams(String name) {
        List<String> list = queryParams().list(name);
        return list == null ? Collections.emptyList() : list;
    }
    default Optional<String> queryParam(String name) {
        return Optional.ofNullable(queryParams().first(name));
    }
    
    MultiList<String> headers();
    List<String> headers(String name);
    Optional<String> header(final String name);
    
    List<Cookie> cookies();
    
    //something handling upload
    MultiList<FormItem> formData();
    
    
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
    default byte[] bytes() throws IOException {
        try (InputStream stream = in()) {
            ByteArrayOutputStream array = new ByteArrayOutputStream(stream.available());
            return array.toByteArray();
        }
    }
    
    
    String ip();
    String protocol();
    
    //boolean secure();
    
//    /**
//     * Upgrade the request to something else...like a web socket.
//     *
//     * @param type Upgrade type.
//     * @param <T> Upgrade type.
//     * @return A instance of the upgrade.
//     * @throws Exception If the upgrade fails or it is un-supported.
//     * @see NativeWebSocket
//     */
    //<T> T upgrade(Class<T> type) throws Exception;
    
    void startAsync(final Executor executor, final Runnable runnable);
    
    //TODO this should be a part of Context
//    /**
//     * This will try to parse the request body nicely into an object.
//     * It determines the parser based on the request type.
//     * 
//     * You can register your own parsers for other request types, if needed.
//     * Take a look at: {@link ParserEngine} and {@link ParserEngineManager}
//     * 
//     * @param clazz A representation of the expected body
//     * @return The parsed request object, or <code>throws</code> if the body could not be correctly deserialized,
//     *         or the media type was incorrect.
//     * @throws ParsableException If the parsing from the given content type to class failed
//     * @throws MediaTypeException If the media type of the request was not specified
//     */
//    <T> T parseBody() throws Exception;
    //<T> T parseBody(Class<T> clazz) throws ParsableException, MediaTypeException;
}
