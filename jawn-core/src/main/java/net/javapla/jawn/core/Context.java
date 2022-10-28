package net.javapla.jawn.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import net.javapla.jawn.core.util.MultiList;

public interface Context {
    
    interface Request {
        HttpMethod httpMethod();
        String path();
        String queryString();
        Value header(String name);
        
        default boolean accepts(MediaType contentType) {
            Value accept = header("Accept");
            if (accept.isMissing()) {
                return false;
            }
            return Objects.equals(contentType, MediaType.valueOf(accept.value()));
        }
        
        default MediaType contentType() {
            Value header = header("Content-Type");
            return header.isMissing() ? null : MediaType.valueOf(header.value());
        }
        
        
        MultiList<FormItem> form();
        MultiList<FormItem> multipart();
    }
    
    interface Response {
        Value header(String name);
        Response header(String name, String value);
        Response status(int statusCode);
        int status();
        Response removeHeader(String name);
        Response contentType(MediaType type);
        Response charset(Charset encoding);
        
        default Response status(Status status) {
            return status(status.value());
        }
        
        OutputStream stream();
        
        Response respond(Status status);
        Response respond(ByteBuffer data);
        Response respond(InputStream stream);
        Response respond(FileChannel channel);
        
        default Response respond(byte[] data) {
            return respond(ByteBuffer.wrap(data));
        }
        
        boolean isResponseStarted();
    }
    
    Request req();
    Response resp();
    

    interface FormItem extends Closeable {
        String name();
        Optional<String> value();
        Optional<FileUpload> file();
        
        @Override
        default void close() {
            file().ifPresent(file -> {
                try {
                    file.close();
                } catch (IOException e) {
                    throw Up.IO(e);
                }
            });
        }
    }
    
    interface FileUpload extends Closeable {

        InputStream stream();
        byte[] bytes();
        long fileSize();
        String fileName();
        Path path();
        String contentType();
        
    }
}
