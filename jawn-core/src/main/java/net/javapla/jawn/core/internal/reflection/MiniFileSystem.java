package net.javapla.jawn.core.internal.reflection;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public interface MiniFileSystem {

    /**
     * Recursively list all directories
     * 
     * @param root
     * @return
     * @throws IOException 
     */
    List<Path> listDirectories(Path root);
    
    void watchForChanges(Path dir, WatchService service) throws IOException;
    
    
    
    static MiniFileSystem newMiniFileSystem() {
        return new MiniFileSystem() {
            
            @Override
            public void watchForChanges(Path dir, WatchService service) throws IOException {
                dir.register(service, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE); // watch for an existing file gets modified or directory added
            }
            
            @Override
            public List<Path> listDirectories(Path root) {
                ArrayList<Path> arr = new ArrayList<>();
                try {
                    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            arr.add(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException ignore) {} // ought never be possible, as it then would be ArrayList#add that threw something 
                
                arr.trimToSize();
                return arr;
            }
        };
    }
}
