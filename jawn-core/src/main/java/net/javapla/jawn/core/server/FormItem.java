package net.javapla.jawn.core.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    String fieldName();
    
    /**
     * @return A string if this is a simple value
     */
    Optional<String> value();

    /**
     * This might be a file - might just be a simple string value
     * @return
     * @throws IOException
     */
    Optional<Path> file() throws IOException;
    
    Optional<InputStream> stream() throws IOException;
    
    Optional<String> fileName();
    
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
        InputStream stream = stream().orElseThrow(() -> new IOException("No file found for " + fieldName()));
        try (stream) {
            return StreamUtil.bytes(stream);
        }
    }


    /**
     * Saves content of this item to a file.
     *
     * @param output to file
     * @throws IOException
     */
    default void saveTo(Path output) throws IOException {
        //Path file = file().orElseThrow(() -> new IOException("No file found for " + name()));
        //Files.copy(file.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        saveTo(output, StandardCopyOption.REPLACE_EXISTING);
    }
    
    default void saveTo(Path output, CopyOption ... options) throws IOException {
        if (file().isPresent()) {
            try {
                Files.move(file().get(), output, options);
                return;
            } catch (IOException e) {
                // ignore and let the Files.copy, outside
                // this if block, take over and attempt to copy it
            }
        }
        
        if (stream().isPresent()) {
            try (InputStream is = stream().get()) {
                Files.copy(is, output, options);
                return;
            }
        }
        
        throw new IOException("No file found for " + fieldName());
    }

    @Override
    default void close() throws IOException {
        file().ifPresent(file -> file.toFile().delete());
    }
}
