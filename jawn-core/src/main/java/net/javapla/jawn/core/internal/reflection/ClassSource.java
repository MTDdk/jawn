package net.javapla.jawn.core.internal.reflection;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.util.StreamUtil;

public class ClassSource implements Closeable {

    private final WeakHashMap<String, byte[]> bytecode = new WeakHashMap<>();
    final ClassLoader loader;
    
    public ClassSource(ClassLoader loader) {
        this.loader = loader;
    }
    public ClassSource() {
        this(Thread.currentThread().getContextClassLoader());
    }
    
    public byte[] byteCode(Class<?> source) {
        
        return bytecode.computeIfAbsent(source.getName(), name -> {
            try (InputStream in = loader.getResourceAsStream(name.replace('.', '/') + ".class")) {
                
                if (in == null) {
                    // Class not found
                    throw Up.IO(new ClassNotFoundException(name));
                }
                
                return StreamUtil.bytes(in);
            } catch (IOException e) {
                throw Up.IO(e);
            }
        });
    }
    
    public void reload(Class<?> source) {
        bytecode.remove(source.getName());
    }
    
    @Override
    public void close() {
        bytecode.clear();
    }
}
