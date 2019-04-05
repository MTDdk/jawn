package net.javapla.jawn.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.util.StreamUtil;

public interface BodyValue extends Value {
    

    byte[] bytes() throws Up.IO;
    
    long length() throws Up.IO;
    
    boolean isInMemory();
    
    InputStream stream() throws Up.IO;
    
    <T> T to(Class<T> type);
    
//    @Override
    default String value() {
        return value(StandardCharsets.UTF_8);
    }
    
    default String value(final Charset charset) {
        return new String(bytes(), charset);
    }
    
//    @Override
    default boolean isPresent() {
        return length() > 0;
    }
    
    
    static BodyValue empty() {
        return ByteArrayBody.EMPTY;
    }
    
    static BodyValue of(final ParserEngine engine, final byte[] bytes) {
        return new ByteArrayBody(engine, bytes);
    }
    
    static BodyValue of(final ParserEngine engine, final Path file) {
        return new FileBody(engine, file);
    }
    
    static BodyValue of(final ParserEngine engine, final InputStream in, final long contentLength) {
        return new InputStreamBody(engine, in, contentLength);
    }
    
    
    static class ByteArrayBody implements BodyValue {
        public static final BodyValue EMPTY = new ByteArrayBody(null, new byte[0]);
        
        private final ParserEngine engine;
        private final byte[] bytes;
        
        ByteArrayBody(final ParserEngine engine, final byte[] bytes) {
            this.engine = engine;
            this.bytes = bytes;
        }
        
        @Override
        public byte[] bytes() {
            return bytes;
        }
        
        @Override
        public long length() {
            return bytes.length;
        }
        
        @Override
        public boolean isInMemory() {
            return true;
        }
        
        @Override
        public InputStream stream() {
            return new ByteArrayInputStream(bytes);
        }
        
        @Override
        public <T> T to(Class<T> type) {
            return engine.invoke(bytes, type);
        }
        
    }
    
    static class FileBody implements BodyValue {
        private final ParserEngine engine;
        private final Path file;

        FileBody(final ParserEngine engine, final Path file) {
            this.engine = engine;
            this.file = file;
        }
        
        @Override
        public byte[] bytes() throws Up.IO {
            try {
                return Files.readAllBytes(file);
            } catch (IOException e) {
                throw new Up.IO(e);
            }
        }
        
        @Override
        public long length() {
            try {
                return Files.size(file);
            } catch (IOException e) {
                throw new Up.IO(e);
            }
        }
        
        @Override
        public boolean isInMemory() {
            return false;
        }
        
        @Override
        public InputStream stream() {
            try {
                return Files.newInputStream(file);
            } catch (IOException e) {
                throw new Up.IO(e);
            }
        }
        
        @Override
        public <T> T to(Class<T> type) {
            return engine.invoke(stream(), type);
        }
    }
    
    static class InputStreamBody implements BodyValue {
        private final ParserEngine engine;
        private final InputStream stream;
        private final long length;

        InputStreamBody(final ParserEngine engine, final InputStream stream, final long contentLength) {
            this.engine = engine;
            this.stream = stream;
            this.length = contentLength;
        }
        
        @Override
        public byte[] bytes() throws Up.IO {
            try {
                return StreamUtil.bytes(stream);
            } catch (IOException e) {
                throw new Up.IO(e);
            }
        }
        
        @Override
        public long length() {
            return length;
        }
        
        @Override
        public boolean isInMemory() {
            return false;
        }
        
        @Override
        public InputStream stream() {
            return stream;
        }
        
        @Override
        public <T> T to(Class<T> type) {
            return engine.invoke(stream, type);
        }
    }
}
