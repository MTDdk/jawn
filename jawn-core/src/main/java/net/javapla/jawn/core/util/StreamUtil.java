package net.javapla.jawn.core.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StreamUtil {

    /**
     * Reads contents of the input stream fully and returns it as byte array.
     *
     * @param in InputStream to read from.
     * @return contents of the input stream fully as byte array
     * @throws IOException in case of IO error
     * @throws IllegalArgumentException if stream is null
     */
    public static byte[] bytes(InputStream in) throws IOException {
        if(in == null)
            throw new IllegalArgumentException("input stream cannot be null");

        ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[1024];

        for (int x = in.read(bytes); x != -1; x = in.read(bytes))
            bout.write(bytes, 0, x);

        return bout.toByteArray();
    }
    
    /**
     * Reads contents of the input stream fully and returns it as String. Sets UTF-8 encoding internally.
     *
     * @param in InputStream to read from.
     * @return contents of the input stream fully as String.
     * @throws IOException in case of IO error
     * @throws IllegalArgumentException if stream is null
     */
    public static String read(InputStream in) throws IOException {
        return read(in, StandardCharsets.UTF_8);
    }


    /**
     * Reads contents of the input stream fully and returns it as String.
     *
     * @param in InputStream to read from.
     * @param charset name of supported charset to use
     * @return contents of the input stream fully as String.
     * @throws IOException in case of IO error
     * @throws IllegalArgumentException if stream is null
     */
    public static String read(InputStream in, Charset charset) throws IOException {
        if(in == null)
            throw new IllegalArgumentException("input stream cannot be null");

        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();

        for (int x = reader.read(buffer); x != -1; x = reader.read(buffer)) {
            sb.append(buffer, 0, x);
        }
        return sb.toString();
    }
    
    /**
     * Saves content read from input stream into a file.
     *
     * @param path path to file.
     * @param in  input stream to read content from.
     * @throws IOException IO error
     * @throws IllegalArgumentException if stream is null or path is null
     */
    public static void saveTo(String path, InputStream in) throws IOException {
        if (in == null)
            throw new IllegalArgumentException("input stream cannot be null");
        if (path == null)
            throw new IllegalArgumentException("path cannot be null");

        try (OutputStream out = new BufferedOutputStream(
                                        Files.newOutputStream(
                                                Paths.get(path), 
                                                StandardOpenOption.CREATE, 
                                                StandardOpenOption.APPEND))) {
            byte[] bytes = new byte[1024];
            for (int x = in.read(bytes); x != -1; x = in.read(bytes))
                out.write(bytes, 0, x);
        }
    }
}
