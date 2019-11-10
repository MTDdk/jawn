package net.javapla.jawn.core.internal.reflection;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

import org.junit.BeforeClass;
import org.junit.Test;

public class JarLoaderTest {
    
    private static final Path resources = Paths.get("src", "test", "resources");
    
    static ClassLoader loader;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        loader = mock(ClassLoader.class);
        when(loader.getResources(eq("webapp/views/system"))).thenReturn(Collections.enumeration(List.of(new URL("jar:file:" + resources.resolve("test-jawn-templates.jar!/webapp")))));
    }

    @Test
    public void getResourcesFromJarFile() throws ZipException, IOException {
        JarLoader jarLoader = new JarLoader(loader, "webapp/views/system");
        
        Enumeration<URL> enumeration = jarLoader.getResourcesFromJarFile();
        assertThat(enumeration).isNotNull();
        assertThat(enumeration.hasMoreElements()).isTrue();
        
        int count = 0;
        while(enumeration.hasMoreElements()) {
            enumeration.nextElement();
            //System.out.println(element);
            count++;
        }
        assertThat(count).isEqualTo(5);
        
        jarLoader.close();
    }

    
}
