package net.javapla.jawn.core.http;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import net.javapla.jawn.core.util.MultiList;

public interface Req {
    
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
    //files(String name) throws Exception;
    InputStream in() throws Exception;
    String ip();
    String protocol();
    boolean secure();
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
