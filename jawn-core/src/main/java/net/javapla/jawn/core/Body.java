package net.javapla.jawn.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.javapla.jawn.core.util.StreamUtil;

public interface Body extends Value {
    
    byte[] bytes();

    long size();
    
    boolean inMemory();
    
    InputStream stream();
    
    ReadableByteChannel channel();
    
    default String value(Charset charset) {
        return new String(bytes(), charset);
    }
    
    @Override
    default String value() {
        return value(StandardCharsets.UTF_8);
    }
    
    @Override
    default boolean isPresent() {
        return true;
    }
    
    
    /* 
     * Factories
     */
    
    static Body empty() {
        return EMPTY;
    }
    
    static Body of(final InputStream stream, final long contentLength) {
        return new Body() {

            @Override
            public byte[] bytes() {
                try {
                    return StreamUtil.bytes(stream);
                } catch (IOException e) {
                    throw Up.IO(e);
                }
            }

            @Override
            public long size() {
                return contentLength;
            }
            
            @Override
            public boolean inMemory() {
                return false;
            }

            @Override
            public InputStream stream() {
                return stream;
            }

            @Override
            public ReadableByteChannel channel() {
                return Channels.newChannel(stream);
            }

        };
    }
    
    static Body of(final Path file) {
        return new Body() {

            @Override
            public byte[] bytes() {
                try {
                    return Files.readAllBytes(file);
                } catch (IOException e) {
                    throw Up.IO(e);
                }
            }

            @Override
            public long size() {
                try {
                    return Files.size(file);
                } catch (IOException e) {
                    throw Up.IO(e);
                }
            }

            @Override
            public boolean inMemory() {
                return false;
            }

            @Override
            public InputStream stream() {
                try {
                    return Files.newInputStream(file);
                } catch (IOException e) {
                    throw Up.IO(e);
                }
            }

            @Override
            public ReadableByteChannel channel() {
                try {
                    return Files.newByteChannel(file);
                } catch (IOException e) {
                    throw Up.IO(e);
                }
            }
        };
    }
    
    
    
    static Body of(final byte[] bytes) {
        return new ByteArrayBody(bytes);
    }
    
    final static Body EMPTY = of(new byte[0]);

    static class ByteArrayBody implements Body {
        private final byte[] bytes;
    
        ByteArrayBody(byte[] bytes) {
            this.bytes = bytes;
        }
    
        @Override
        public byte[] bytes() {
            return bytes;
        }
    
        @Override
        public long size() {
            return bytes.length;
        }
    
        @Override
        public boolean inMemory() {
            return true;
        }
    
        @Override
        public InputStream stream() {
            return new ByteArrayInputStream(bytes);
        }
    
        @Override
        public ReadableByteChannel channel() {
            return Channels.newChannel(stream());
        }
    
        @Override
        public boolean isPresent() {
            return bytes.length > 0;
        }
    }
}
