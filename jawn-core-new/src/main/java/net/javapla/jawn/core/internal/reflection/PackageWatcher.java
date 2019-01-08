package net.javapla.jawn.core.internal.reflection;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Consumer;

import net.javapla.jawn.core.Err.Compilation;
import net.javapla.jawn.core.Err.UnloadableClass;
import net.javapla.jawn.core.Jawn;

public class PackageWatcher implements Closeable {
    
    private final String jawnInstanceClassName;
    private final String implementorsPackageName;
    private final Path packagePath;
    private final Consumer<Jawn> reloader;
    
    private WatchService service;
    private volatile boolean running = false;
    
    public PackageWatcher(final Class<? extends Jawn> jawn, final Consumer<Jawn> reloader) {
        this.jawnInstanceClassName = jawn.getSimpleName() + ".class";
        this.implementorsPackageName = jawn.getPackageName();
        this.packagePath = Paths.get(jawn.getResource("").getFile());
        this.reloader = reloader;
    }

    public void start() throws IOException, InterruptedException {
        service = FileSystems.getDefault().newWatchService();
        packagePath.register(service, StandardWatchEventKinds.ENTRY_MODIFY); //currently, only when an existing file gets modified
        
        new Thread() {
            @Override
            public void run() {
                running = true;
                WatchKey key;
                
                try {
                    while (running && (key = service.take()) != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.context() != null) {
                                Path p = (Path) event.context();
                                
                                // it might be something like: bin/test/implementation/JawnMainTest$1.class
                                //if (p.getFileName().toString().indexOf('$') > 0) continue;
                                // we might actually want this..
                                
                                if (p.endsWith(jawnInstanceClassName)) {
                                    // do something special when it is the Jawn instance
                                    
                                    Jawn instance = DynamicClassFactory.createInstance(DynamicClassFactory.getCompiledClass(implementorsPackageName + '.' + p.getFileName(), false), Jawn.class);
                                    reloader.accept(instance);
                                } else {
                                    // perhaps something different.. ?
                                }
                                
                                System.out.println(packagePath.resolve(p));
                            }
                        }
                        key.reset();
                    }
                } catch (UnloadableClass | Compilation | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void close() throws IOException {
        running = false;
        if (service != null) {
            service.close();
        }
    }
    
}
