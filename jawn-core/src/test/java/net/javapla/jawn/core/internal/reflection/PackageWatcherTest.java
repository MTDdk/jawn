package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import net.javapla.jawn.core.Jawn;
import test.classlocator.TestJawnClass;

public class PackageWatcherTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws IOException, InterruptedException, ExecutionException {
        
        BiConsumer<Jawn, Class<?>> jawnConsumer = mock(BiConsumer.class);
        
        WatchService watchService = mock(WatchService.class);
        MiniFileSystem miniFileSystem = mock(MiniFileSystem.class);
        
        when(miniFileSystem.listDirectories(any(Path.class))).thenAnswer(AdditionalAnswers.answer((Path root) -> {
            return List.of(root);
        }));
        
        
        
        PackageWatcher watcher = new PackageWatcher(watchService, miniFileSystem, TestJawnClass.class, jawnConsumer);
        
        assertThat(watcher.isRunning()).isFalse();
        
        Path path = watcher.getWatchingFileSystemPath();
        assertThat((Object)path).isNotNull();
        assertThat(path.endsWith("test/classlocator"));
        
        CompletableFuture<WatchKey> completableFuture = new CompletableFuture<WatchKey>();
        
        when(watchService.take())
            .then(invocation -> completableFuture.get()) // first call
            .then(invocation -> { TimeUnit.SECONDS.sleep(100); return null;}); // second call - sleep "forever" in order for the thread to get stopped
        
        watcher.start();
        assertThat(watcher.isRunning()).isTrue();
        assertThat(watcher.watchedDirs()).hasSize(1); // endsWith("test/classlocator")
        
        
        WatchKey watchKey = mock(WatchKey.class);
        WatchEvent<Path> watchEvent = mock(WatchEvent.class);
        when(watchEvent.context()).thenReturn(Paths.get("LocatableClass1.class"));
        when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
        when(watchKey.watchable()).thenReturn(path);
        when(watchKey.pollEvents()).thenReturn(List.of(watchEvent));
        
        
        //File.createTempFile("testclass", null, null);
        completableFuture.complete(watchKey);
        TimeUnit.MILLISECONDS.sleep(600); // wait for the thread to pick up the watchkey
        
        
        watcher.close();
        assertThat(watcher.isRunning()).isFalse();
        assertThat(watcher.watchedDirs()).isEmpty();
        
        verify(miniFileSystem, times(1)).watchForChanges(path, watchService);
        verify(jawnConsumer, times(1)).accept(any(), any());
        verify(watchService, times(1)).close();
    }
    

}
