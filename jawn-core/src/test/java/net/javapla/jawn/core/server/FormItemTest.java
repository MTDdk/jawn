package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

public class FormItemTest {

    @Test
    public void bytes() throws IOException {
        FormItem item = mock(FormItem.class);
        when(item.bytes()).thenCallRealMethod();
        
        File file = mock(File.class);
        when(file.toPath()).thenReturn(Paths.get("src", "test", "resources", "webapp", "css", "dummy.css"));
        when(item.file()).thenReturn(Optional.of(file));
        
        assertThat(item.bytes()).isEqualTo("body { height: 73px; }".getBytes());
    }
    
    @Test
    public void bytes_throw() throws IOException {
        FormItem item = mock(FormItem.class);
        when(item.bytes()).thenCallRealMethod();
        when(item.file()).thenReturn(Optional.empty());
        
        assertThrows(IOException.class, () -> item.bytes());
    }
    
    @Test
    public void close() throws IOException {
        FormItem item = mock(FormItem.class);
        File file = mock(File.class);
        when(item.file()).thenReturn(Optional.of(file));
        doCallRealMethod().when(item).close();
        
        try (item) { }
        
        verify(item, times(1)).close();
        verify(file, times(1)).delete();
    }

    @Test
    public void close_empty() throws IOException {
        FormItem item = mock(FormItem.class);
        when(item.file()).thenReturn(Optional.empty());
        doCallRealMethod().when(item).close();
        
        try (item) { }
        
        verify(item, times(1)).close();
    }
    
    @Test
    @Ignore("should be some kind of integration test")
    public void saveTo() throws IOException {
        FormItem item = mock(FormItem.class);
        doCallRealMethod().when(item).saveTo(any());
        
        File file = mock(File.class);
        when(file.toPath()).thenReturn(Paths.get("src", "test", "resources", "webapp", "css", "dummy.css"));
        when(item.file()).thenReturn(Optional.of(file));
        
        File output = mock(File.class);
        when(output.toPath()).thenReturn(Paths.get("src", "test", "resources", "webapp", "css", "dummy2.css"));
        
        item.saveTo(output);
    }
}
