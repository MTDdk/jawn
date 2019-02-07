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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Up.Compilation;
import net.javapla.jawn.core.Up.UnloadableClass;

public class PackageWatcher implements Closeable {
    
    private final String jawnInstanceClassName;
    private final String jawnInstancePackageClass;
    private final Path packagePath;
    private final BiConsumer<Jawn, Class<?>> reloader;
    
    private WatchService service;
    private volatile boolean running = false;
    
    public PackageWatcher(final Class<? extends Jawn> jawn, final BiConsumer<Jawn, Class<?>> reloader) {
        this.jawnInstanceClassName = jawn.getSimpleName() + ".class";
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
        //System.out.println("Watching dir " + dir);
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
                    locateChangedDir(p, changedDir -> {
                        // If an event is a StandardWatchEventKinds.ENTRY_CREATE
                        // and path is a directory, then create a watcher for that
                        // new dir as well
                        try {
                            registerDir(changedDir, service);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    
                    break;
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    
                    
                    Path changedFile = locateChangedFile(p);
                    Class<?> c = null;
                    
                    // We always reload the Jawn-instance, so skip it here
                    if (!changedFile.endsWith(jawnInstanceClassName)) {
                        
                        //convert to a class file
                        System.out.println(changedFile);
                        System.out.println(packagePath);
                        
                        String substring = changedFile.toString().substring(packagePath.toString().length()+1);
                        System.out.println(substring);
                        System.out.println(packagePath.getFileName().resolve(substring).toString().replace('/', '.'));
                        
                        
                        c = reloadClass(packagePath.getFileName().resolve(substring).toString().replace('/', '.'));
                    }
                    
                    
                    // Always reload MainJawn
                    reloadMainJawn(c);
                    
                    
                    // It turns out that reloading Jawn in turn will reload any
                    // dependent classes, so we do not need to handle each of the
                    // modified classes
                    break;
                }
            }
        }
        key.reset();
    }
    
    private Path locateChangedFile(final Path p) throws IOException {
        Path[] k = new Path[1];
        
        // Try to locate the file in the directory structure
        Files.walkFileTree(packagePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                boolean equals = file.getFileName().equals(p);
                if (equals) {
                    k[0] = file;
                    return FileVisitResult.TERMINATE;
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
        
        return k[0];
    }
    
    private void locateChangedDir(final Path p, Consumer<Path> handle) throws IOException {
        Files.walkFileTree(packagePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                boolean equals = dir.getFileName().equals(p);
                if (equals) {
                    handle.accept(dir);
                    return FileVisitResult.TERMINATE;
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    private void reloadMainJawn(final Class<?> reloadedClass) /*throws Up.Compilation*/ {
        try {
            Jawn instance = ClassFactory.createInstance(reloadClass(jawnInstancePackageClass), Jawn.class);
            reloader.accept(instance, reloadedClass);
        } catch (UnloadableClass | Compilation e) {
            e.printStackTrace();                                        
        }
    }
    
    private Class<?> reloadClass(String className) {
        boolean cacheTheClassFile = false;
        return ClassFactory.getCompiledClass(className, cacheTheClassFile);
    }

    @Override
    public void close() throws IOException {
        running = false;
        if (service != null) {
            service.close();
        }
    }
    
}
