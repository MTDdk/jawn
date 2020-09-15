package net.javapla.jawn.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.javapla.jawn.core.util.StreamUtil;

public interface Body {

    byte[] bytes();
    
    //boolean inMemory();
    
    long size();
    
    InputStream stream();
    
    
    static Body of(/*final Context context, */final InputStream stream, final long contentLength) {
        return new Body() {
            
            @Override
            public byte[] bytes() {
                try {
                    return StreamUtil.bytes(stream);
                } catch (IOException e) {
                    throw Up.IO.because(e);
                }
            }
            
            @Override
            public InputStream stream() {
                return stream;
            }
            
            @Override
            public long size() {
                return contentLength;
            }
            
            /*@Override
            public boolean inMemory() {
                return false;
            }*/
        };
    }
    
    static Body of(/*final Context context, */final byte[] bytes) {
        return new Body() {
            
            @Override
            public byte[] bytes() {
                return bytes;
            }
            
            @Override
            public InputStream stream() {
                return new ByteArrayInputStream(bytes);
            }
            
            @Override
            public long size() {
                return bytes.length;
            }
            
            /*@Override
            public boolean inMemory() {
                return true;
            }*/
        };
    }
}
