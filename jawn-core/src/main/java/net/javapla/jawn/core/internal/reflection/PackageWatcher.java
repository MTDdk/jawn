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

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Up;

public class PackageWatcher implements Closeable {
    
    private final String jawnInstanceClassName;
    private final String jawnInstancePackageClass;
    private final Path packageFileSystemPath;
    private final BiConsumer<Jawn, Class<?>> reloader;
    
    private WatchService service;
    private volatile boolean running = false;
    
    public PackageWatcher(final Class<? extends Jawn> jawn, final BiConsumer<Jawn, Class<?>> reloader) {
        this.jawnInstanceClassName = jawn.getSimpleName() + ".class";
        this.jawnInstancePackageClass = jawn.getName();
        this.packageFileSystemPath = Paths.get(jawn.getResource("").getFile());
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
        registerDirAndSubDirectories(packageFileSystemPath, service);
        
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
                    Path changedDir = locateChangedDir(p);
                    
                    if (changedDir != null) {
                        // If an event is a StandardWatchEventKinds.ENTRY_CREATE
                        // and path is a directory, then create a watcher for that
                        // new dir as well
                        registerDir(changedDir, service);
                    }
                    
                    break;
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    
                    Path changedFile = locateChangedFile(p);
                    Class<?> c = null;
                    
                    // We always reload the Jawn-instance, so skip it here
                    if (!changedFile.endsWith(jawnInstanceClassName)) {
                        
                        // find the package path by removing the absolute path
                        String packageClassPath = changedFile.toString().substring(packageFileSystemPath.toString().length()+1);
                        
                        //convert to a class file and reload it from filesystem
                        try {
                            c = reloadClass(packageFileSystemPath.getFileName().resolve(packageClassPath).toString().replace('/', '.'));
                        } catch (Up.Compilation | Up.UnloadableClass e) {
                            e.printStackTrace();
                        }
                        
                        /* 
                         * Example:
                         * 
                         * packageFileSystemPath = /home/user/project/bin/main/app
                         * changedFile           = /home/user/project/bin/main/app/controller/Class.class
                         * packageClassPath      = controller/Class.class
                         * packageFileSystemPath.getFileName() = app
                         * class name            = app.controller.Class.class 
                         */
                    }
                    
                    
                    // Always reload Jawn-instance
                    reloadMainJawn(c);
                    
                    break;
                }
            }
        }
        key.reset();
    }
    
    private Path locateChangedFile(final Path p) throws IOException {
        Path[] k = new Path[1];
        
        // Try to locate the file in the directory structure
        Files.walkFileTree(packageFileSystemPath, new SimpleFileVisitor<Path>() {
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
    
    private Path locateChangedDir(final Path p) throws IOException {
        Path[] k = new Path[1];
        
        Files.walkFileTree(packageFileSystemPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                boolean equals = dir.getFileName().equals(p);
                if (equals) {
                    k[0] = dir;
                    return FileVisitResult.TERMINATE;
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
        
        return k[0];
    }
    
    private void reloadMainJawn(final Class<?> reloadedClass) /*throws Up.Compilation*/ {
        try {
            Jawn instance = ClassFactory.createInstance(reloadClass(jawnInstancePackageClass), Jawn.class);
            reloader.accept(instance, reloadedClass);
        } catch (Up.UnloadableClass | Up.Compilation e) {
            e.printStackTrace();                                        
        }
    }
    
    private Class<?> reloadClass(String className) throws Up.Compilation, Up.UnloadableClass {
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
