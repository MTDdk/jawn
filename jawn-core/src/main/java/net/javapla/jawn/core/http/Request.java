package net.javapla.jawn.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import net.javapla.jawn.core.uploads.FormItem;
import net.javapla.jawn.core.util.MultiList;

public interface Request {
    
    HttpMethod method();
    String path();
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
}
