package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class MiniFileSystemTest {
    
    @Test
    public void test() {
        MiniFileSystem system = MiniFileSystem.newMiniFileSystem();
        
        Path root = Paths.get("src", "test", "java", "test");
        
        List<Path> list = system.listDirectories(root);
        
        assertThat(list).hasSize(3);
        assertThat(list).containsExactly(root, root.resolve("classlocator"), root.resolve("classlocator").resolve("extralayer"));
    }

}
