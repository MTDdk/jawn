package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class FormItemTest {

    @Test
    @Ignore
    public void bytes() throws IOException {
        FormItem item = mock(FormItem.class);
        when(item.bytes()).thenCallRealMethod();
        
        File file = mock(File.class);
        when(item.file()).thenReturn(Optional.of(file));
        
        assertThat(item.bytes());
    }
    
    @Test
    public void bytes_throw() throws IOException {
        FormItem item = mock(FormItem.class);
        when(item.bytes()).thenCallRealMethod();
        when(item.file()).thenReturn(Optional.empty());
        
        assertThrows(IOException.class, () -> item.bytes());
    }

}
