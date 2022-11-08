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
    
    public byte[] byteCode(Class<?> source) {
        return bytecode.computeIfAbsent(source.getName(), name -> {
            try (InputStream in = loader.getResourceAsStream(name.replace('.', '/') + ".class")) {
                return StreamUtil.bytes(in);
            } catch (IOException e) {
                throw Up.IO(e);
            }
        });
    }
    
    @Override
    public void close() {
        bytecode.clear();
    }
}
