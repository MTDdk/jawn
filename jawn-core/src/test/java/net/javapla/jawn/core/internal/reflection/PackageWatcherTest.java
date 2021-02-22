package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;

import net.javapla.jawn.core.Jawn;
import test.classlocator.TestJawnClass;

public class PackageWatcherTest {
    
    @Test
    public void watchingFileSystemPath() {
        @SuppressWarnings("unchecked")
        PackageWatcher watcher = new PackageWatcher(mock(WatchService.class), mock(MiniFileSystem.class), TestJawnClass.class, mock(BiConsumer.class));
        
        assertThat(watcher.isRunning()).isFalse();
        
        Path path = watcher.getWatchingFileSystemPath();
        assertThat((Object)path).isNotNull();
        assertThat(path.endsWith("test/classlocator"));
    }

    @Test
    public void test() throws IOException, InterruptedException, ExecutionException {
        alterSingleClass(null, Path.of("LocatableClass1.class"), test.classlocator.LocatableClass1.class);
    }
    
    @Test
    public void test_with_extraPackage() throws IOException, InterruptedException, ExecutionException {
        alterSingleClass("extralayer", Path.of("LocatableClass4.class"), test.classlocator.extralayer.LocatableClass4.class);
    }

    
    @SuppressWarnings("unchecked")
    private void alterSingleClass(String filePath, Path classFile, Class<?> listeningFor) throws InterruptedException, IOException {
        BiConsumer<Jawn, Class<?>> jawnConsumer = mock(BiConsumer.class);
        
        WatchService watchService = mock(WatchService.class);
        MiniFileSystem miniFileSystem = mock(MiniFileSystem.class);
        
        when(miniFileSystem.listDirectories(any(Path.class))).thenAnswer(AdditionalAnswers.answer((Path root) -> {
            return List.of(root);
        }));
        
        
        
        PackageWatcher watcher = new PackageWatcher(watchService, miniFileSystem, TestJawnClass.class, jawnConsumer);
        
        
        CompletableFuture<WatchKey> completableFuture = new CompletableFuture<WatchKey>();
        CountDownLatch latch = new CountDownLatch(2);
        
        when(watchService.take())
            .then(invocation -> {latch.countDown(); return completableFuture.get();}) // first call
            .then(invocation -> {latch.countDown(); TimeUnit.SECONDS.sleep(100); return null;}); // second call - sleep "forever" in order for the thread to wait for more input
        
        watcher.start();
        assertThat(watcher.isRunning()).isTrue();
        assertThat(watcher.watchedDirs()).hasSize(1); // endsWith("test/classlocator")
        
        
        Path path = watcher.getWatchingFileSystemPath();
        WatchKey watchKey = mock(WatchKey.class);
        WatchEvent<Path> watchEvent = mock(WatchEvent.class);
        when(watchEvent.context()).thenReturn(classFile);
        when(watchEvent.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
        when(watchKey.watchable()).thenReturn((filePath != null) ? path.resolve(filePath) : path);
        when(watchKey.pollEvents()).thenReturn(List.of(watchEvent));
        
        
        //File.createTempFile("testclass", null, null);
        completableFuture.complete(watchKey);
        latch.await(); // wait for the thread to pick up the watchkey
        
        
        watcher.close();
        assertThat(watcher.isRunning()).isFalse();
        assertThat(watcher.watchedDirs()).isEmpty();
        verify(miniFileSystem, times(1)).watchForChanges(path, watchService);
        verify(watchService, times(1)).close();
        
        ArgumentCaptor<Class<?>> cc = ArgumentCaptor.forClass(Class.class);
        verify(jawnConsumer, times(1)).accept(any(), cc.capture());
        assertThat(cc.getValue().toString()).isEqualTo(listeningFor.toString());
    }

}
