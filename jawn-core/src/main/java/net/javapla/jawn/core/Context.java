package net.javapla.jawn.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import net.javapla.jawn.core.util.MultiList;

public interface Context {
    
    // Often used headers
    static final String ACCEPT = "Accept";
    
    interface Request {
        HttpMethod httpMethod();
        String path();
        String queryString();
        Value header(String name);
        
        default boolean accept(MediaType contentType) {
            Value accept = header(ACCEPT);
            if (accept.isMissing()) {
                return false;
            }
            return Objects.equals(contentType, MediaType.valueOf(accept.value()));
        }
        
        long contentLength();
        default MediaType contentType() {
            Value header = header("Content-Type");
            return header.isMissing() ? null : MediaType.valueOf(header.value());
        }
        
        
        MultiList<FormItem> multipart();
        default MultiList<FormItem> form() {
            return multipart();
        }
        
        Body body();
        <T> T parse(Type type);
    }
    
    interface Response {
        static final String STANDARD_HEADER_CONTENT_TYPE = MediaType.PLAIN.name() + ";charset=" + StandardCharsets.UTF_8.name();
        
        Value header(String name);
        Response header(String name, String value);
        Response status(int statusCode);
        int status();
        Response removeHeader(String name);
        MediaType contentType();
        Response contentType(MediaType type);
        Response rendererContentType(MediaType type);
        
        Response charset(Charset encoding);
        Charset charset();
        
        default Response status(Status status) {
            return status(status.value());
        }
        
        OutputStream stream();
        default PrintWriter writer(/*MediaType type, Charset charset*/) {
            return new PrintWriter(new OutputStreamWriter(stream(), charset()));
        }
        
        Response respond(Status status);
        Response respond(ByteBuffer data);
        Response respond(InputStream stream);
        Response respond(FileChannel channel);
        default Response respond(byte[] data) {
            return respond(ByteBuffer.wrap(data));
        }
        default Response respond(String data) {
            return respond(data.getBytes(charset()));
        }
        
        boolean isResponseStarted();
    }
    
    Request req();
    Response resp();
    
    
    // ** Attributes **
    Context attribute(final String name, final Object value);
    Optional<Object> attribute(final String name);
    Context removeAttribute(final String name);
    
    
    // ** Session **
    
    
    // ** Route **
    //Route route();
    default void error(Throwable t) {
        if (t instanceof Up) {
            System.out.println( ((Up)t).getMessage() );
            resp().status(((Up) t).status());
        } else {
            t.printStackTrace();
        }
    }


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
        
        static FormItem of(String path, String value) {
            return new Context.FormItem() {
                
                @Override
                public String name() {
                    return path;
                }

                @Override
                public Optional<String> value() {
                    return Optional.of(value);
                }

                @Override
                public Optional<FileUpload> file() {
                    return Optional.empty();
                }
                
                @Override
                public String toString() {
                    return path + "=" + value;
                }
            };
        }
        
        static FormItem of(String path, FileUpload value) {
            return new Context.FormItem() {

                @Override
                public String name() {
                    return path;
                }

                @Override
                public Optional<String> value() {
                    return Optional.empty();
                }

                @Override
                public Optional<FileUpload> file() {
                    return Optional.of(value);
                }
            };
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
