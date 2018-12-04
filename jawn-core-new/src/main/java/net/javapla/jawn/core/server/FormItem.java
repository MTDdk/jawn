package net.javapla.jawn.core.server;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import net.javapla.jawn.core.util.MultiList;
import net.javapla.jawn.core.util.StreamUtil;

/**
 * This interface represents a file or form item that was received within a
 * <code>multipart/form-data</code> POST request.
 * 
 * @author MTD
 *
 */
public interface FormItem extends Closeable {

    /**
     * @return form field name
     */
    String name();
    
    /**
     * @return A string if this is a simple value
     */
    Optional<String> value();

    /**
     * This might be a file - might just be a simple string value
     * @return
     * @throws IOException
     */
    Optional<File> file() throws IOException;
    
    /**
     * @return The headers that were present in the multipart request, or empty if this was not a multipart request
     */
    MultiList<String> headers();
    

    /**
     * Content type of this form field.
     *
     * @return content type of this form field.
     */
    String contentType();


    /**
     * Reads contents of a file into a byte array at once.
     *
     * @return contents of a file as byte array.
     * @throws IOException 
     */
    default byte[] bytes() throws IOException {
        File file = file().orElseThrow(() -> new IOException("No file found for " + name()));
        try (InputStream stream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
            return StreamUtil.bytes(stream);
        }
    }


    /**
     * Saves content of this item to a file.
     *
     * @param path to file
     * @throws IOException
     */
    default void saveTo(String path) throws IOException {
        File file = file().orElseThrow(() -> new IOException("No file found for " + name()));
        Files.copy(file.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    default void close() throws IOException {
        file().ifPresent(file -> file.delete());
    }
}
