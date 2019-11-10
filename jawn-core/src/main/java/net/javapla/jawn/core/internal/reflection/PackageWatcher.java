package net.javapla.jawn.core.internal.reflection;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Up;

public class PackageWatcher implements Closeable {
    
    private final String jawnInstanceClassName;
    private final String jawnInstancePackageClass;
    private final String jawnInstancePackage;
    private final Path packageFileSystemPath;
    private final BiConsumer<Jawn, Class<?>> reloader;
    //private final Consumer<Path> changed;
    
    private final LinkedList<Path> watchedDirs = new LinkedList<>();
    
    private final WatchService service;
    private final MiniFileSystem miniFS;
    private volatile boolean running = false;
    
    public PackageWatcher(WatchService service, MiniFileSystem miniFS, final Class<? extends Jawn> jawn, final BiConsumer<Jawn, Class<?>> reloader) {
        this.service = service;
        this.miniFS = miniFS;
        
        this.jawnInstanceClassName = jawn.getSimpleName() + ".class";
        this.jawnInstancePackageClass = jawn.getName();
        this.jawnInstancePackage = jawn.getPackageName();
        this.packageFileSystemPath = Paths.get(jawn.getResource("").getPath());
        this.reloader = reloader;
    }
    
    /*public PackageWatcher(final Class<? extends Jawn> jawn, final BiConsumer<Jawn, Class<?>> reloader) {
        this(jawn, reloader, p -> {});
    }
    
    public PackageWatcher(final Class<? extends Jawn> jawn, final BiConsumer<Jawn, Class<?>> reloader, final Consumer<Path> changed) {
        this.jawnInstanceClassName = jawn.getSimpleName() + ".class";
        this.jawnInstancePackageClass = jawn.getName();
        this.jawnInstancePackage = jawn.getPackageName();
        this.packageFileSystemPath = Paths.get(jawn.getResource("").getPath());
        this.reloader = reloader;
        this.changed = changed;
    }*/
    
    public Path getWatchingFileSystemPath() {
        return packageFileSystemPath;
    }
    
    public List<Path> watchedDirs() {
        return Collections.unmodifiableList(watchedDirs);
    }

    private void registerDirAndSubDirectories(final Path root, WatchService service) {
        // register directory and sub-directories
        /*try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    registerDir(dir, service);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        miniFS.listDirectories(root).stream().forEach(dir -> {
            try {
                registerDir(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void registerDir(final Path dir/*, WatchService service*/) throws IOException {
        //dir.register(service, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE); // watch for an existing file gets modified or directory added
        miniFS.watchForChanges(dir, service);
        watchedDirs.add(dir);
        //System.out.println("Watching dir " + dir);
    }
    
    public void start() throws IOException, InterruptedException {
        if (isRunning()) new RuntimeException(this.getClass().getSimpleName() + " already initialised");
        
        //service = FileSystems.getDefault().newWatchService();
        registerDirAndSubDirectories(packageFileSystemPath, service);
        
        CountDownLatch latch = new CountDownLatch(1); // with this we can know when the thread is running
        
        new Thread(getClass().getSimpleName()) {
            @Override
            public void run() {
                running = true;
                WatchKey key = null;
                
                latch.countDown();
                try {
                    while (running && (key = service.take()) != null) {
                        consumeKey(key);
                    }
                } catch (InterruptedException | ClosedWatchServiceException | IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        
        latch.await();
    }
    
    private void consumeKey(WatchKey key) throws IOException {
        // We go through all events in case one of the first ones are faulty or unusable
        for (WatchEvent<?> event : key.pollEvents()) {
            
            if (event.context() != null) {
                Path p = (Path) event.context();
                if (!p.isAbsolute()) {
                    p = ((Path) key.watchable()).resolve(p);
                }

                
                // it can be something like: bin/test/implementation/JawnMainTest$1.class
                if (p.getFileName().toString().indexOf('$') > 0) continue;
                // its declaring class *should* always be a part of the pollEvents as well, 
                // and that is sufficient

                
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path changedDir = p;
                    
                    if (changedDir != null) {
                        // If an event is a StandardWatchEventKinds.ENTRY_CREATE
                        // and path is a directory, then create a watcher for that
                        // new dir as well
                        try {
                            registerDir(changedDir/*, service*/);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    continue;
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    
                    Path changedFile = p;
                    Class<?> c = null;
                    
                    // We always reload the Jawn-instance, so skip it here
                    if (!changedFile.endsWith(jawnInstanceClassName)) {
                        
                        // find the package path by removing the absolute path
                        String packageClassPath = changedFile.toString().substring(packageFileSystemPath.toString().length()+1);
                        
                        //convert to a class file and reload it from filesystem
                        try {
                            c = reloadClass(jawnInstancePackage + '.'  + packageClassPath.replace('/', '.'));
                        } catch (Up.Compilation | Up.UnloadableClass e) {
                            e.printStackTrace();
                            continue;
                        }
                        
                        /* 
                         * Example:
                         * 
                         * 
                         * packageFileSystemPath = /home/user/project/bin/main/com/site/app
                         * changedFile           = /home/user/project/bin/main/com/site/app/controller/Class.class
                         * jawnInstancePackage   = com.site.app
                         * packageClassPath      = controller/Class.class
                         * class name            = com.site.app.controller.Class.class 
                         */
                    }
                    
                    
                    // Always reload Jawn-instance
                    reloadMainJawn(c);
                    
                    continue;
                }
            }
        }
        key.reset();
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
        //boolean cacheTheClassFile = false;
        return ClassFactory.recompileClass(className);//getCompiledClass(className, cacheTheClassFile);
    }

    @Override
    public void close() throws IOException {
        running = false;
        
        if (service != null) {
            service.close();
            watchedDirs.clear();
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
}
