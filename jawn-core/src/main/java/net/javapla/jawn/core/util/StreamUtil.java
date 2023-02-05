package net.javapla.jawn.core.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

public abstract class StreamUtil {
    
    public static final int _4KB = 4096;
    public static final int _8KB = _4KB << 1;
    public static final int _16KB = _8KB << 1;
    
    private StreamUtil() {}

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

        ByteArrayOutputStream bout = new ByteArrayOutputStream(_16KB);
        byte[] bytes = new byte[_16KB];

        int read;
        while ((read = in.read(bytes, 0, bytes.length)) != -1) {
            bout.write(bytes, 0, read);
        }

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

        try (InputStreamReader reader = new InputStreamReader(in, charset)) {
            char[] buffer = new char[_16KB];
            StringBuilder sb = new StringBuilder();
    
            for (int x = reader.read(buffer); x != -1; x = reader.read(buffer)) {
                sb.append(buffer, 0, x);
            }
            return sb.toString();
        }
    }
    
    public static String read(Reader in) throws IOException {
        if (in == null)
            throw new IllegalArgumentException("input stream cannot be null");
        
        try (in) {
            if (in instanceof BufferedReader) {
                return ((BufferedReader) in).lines().collect(Collectors.joining("\n"));
            }
            
            char[] buffer = new char[_16KB];
            StringBuilder sb = new StringBuilder();
    
            for (int x = in.read(buffer); x != -1; x = in.read(buffer)) {
                sb.append(buffer, 0, x);//.append("\n");
            }
            return sb.toString();
        }
    }
    
    /**
     * Saves content read from input stream into a file.
     *
     * @param path path to file.
     * @param in  input stream to read content from.
     * @throws IOException IO error
     * @throws IllegalArgumentException if stream is null or path is null
     */
    public static void saveTo(String path, InputStream in) throws IOException, IllegalArgumentException {
        if (in == null)
            throw new IllegalArgumentException("input stream cannot be null");
        if (path == null)
            throw new IllegalArgumentException("path cannot be null");
        
        copy(in, new BufferedOutputStream(
                        Files.newOutputStream(
                            Paths.get(path), 
                            StandardOpenOption.CREATE, 
                            StandardOpenOption.APPEND)));
    }
    
    public static void saveTo(final File file, final InputStream in) throws IllegalArgumentException, IOException {
        if (file == null)
            throw new IllegalArgumentException("file cannot be null"); 
        if (in == null)
            throw new IllegalArgumentException("input stream cannot be null");
        
        file.getParentFile().mkdirs();
        copy(in, new FileOutputStream(file));
    }
    
    public static void copy(final InputStream in, final OutputStream out) throws IOException {
        try (out) {
            byte[] buff = new byte[_8KB];
            for (int x = in.read(buff); x != -1; x = in.read(buff)) {
                out.write(buff, 0, x);
            }
        }
    }
}