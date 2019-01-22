package net.javapla.jawn.core.internal.reflection;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import net.javapla.jawn.core.Up.Compilation;
import net.javapla.jawn.core.Up.UnloadableClass;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Up;

public class PackageWatcher implements Closeable {
    
//    private final String jawnInstanceClassName;
//    private final String implementorsPackageName;
    private final String jawnInstancePackageClass;
    private final Path packagePath;
    private final Consumer<Jawn> reloader;
    
    private WatchService service;
    private volatile boolean running = false;
    
    public PackageWatcher(final Class<? extends Jawn> jawn, final Consumer<Jawn> reloader) {
//        this.jawnInstanceClassName = jawn.getSimpleName() + ".class";
//        this.implementorsPackageName = jawn.getPackageName();
        this.jawnInstancePackageClass = jawn.getName();
        this.packagePath = Paths.get(jawn.getResource("").getFile());
        this.reloader = reloader;
    }

    private void registerDirAndSubDirectories(final Path root, WatchService service) {
        // register directory and sub-directories
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    registerDir(dir, service);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void registerDir(final Path dir, WatchService service) throws IOException {
        dir.register(service, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        System.out.println("Watching dir " + dir);
    }
    
    public void start() throws IOException, InterruptedException {
        service = FileSystems.getDefault().newWatchService();
        //packagePath.register(service, StandardWatchEventKinds.ENTRY_MODIFY); //currently, only when an existing file gets modified
        registerDirAndSubDirectories(packagePath, service);
        
        new Thread(getClass().getSimpleName()) {
            @Override
            public void run() {
                running = true;
                WatchKey key;
                
                try {
                    while (running && (key = service.take()) != null) {
                        consumeKey(key);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    private void consumeKey(WatchKey key) throws IOException {
        // We go through all events in case one of the first ones are faulty or unusable
        for (WatchEvent<?> event : key.pollEvents()) {
            
            if (event.context() != null) {
                Path p = (Path) event.context();
                
                // it can be something like: bin/test/implementation/JawnMainTest$1.class
                if (p.getFileName().toString().indexOf('$') > 0) continue;
                // its main class *should* always be a part of the pollEvents as well, 
                // and that is sufficient
                
                
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    p = packagePath.resolve(p);
                    
                    if (Files.isDirectory(p)) {
                        // If an event is a StandardWatchEventKinds.ENTRY_CREATE
                        // and path is a directory, then create a watcher for that
                        // new dir as well
                        registerDir(p, service);
                    }
                    
                    break;
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    
                    /*if (p.endsWith(jawnInstanceClassName)) {
                    // do something special when it is the Jawn instance
                    } else {
                        // perhaps something different.. ?
                        //DynamicClassFactory.createInstance(DynamicClassFactory.getCompiledClass(implementorsPackageName + '.' + p.getFileName(), false));
                    }*/
                    
                    System.out.println(packagePath.resolve(p));
                    
                    // Always reload MainJawn
                    reloadMainJawn();
                    
                    
                    // It turns out that reloading Jawn in turn will reload any
                    // dependent classes, so we do not need to handle each of the
                    // modified classes
                    break;
                }
            }
        }
        key.reset();
    }
    
    private void reloadMainJawn() throws Up.Compilation {
        try {
            boolean cacheTheClassFile = false;
            Jawn instance = DynamicClassFactory
                .createInstance(DynamicClassFactory.getCompiledClass(jawnInstancePackageClass, cacheTheClassFile), Jawn.class);
            reloader.accept(instance);
        } catch (UnloadableClass | Compilation e) {
            e.printStackTrace();                                        
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        if (service != null) {
            service.close();
        }
    }
    
}
