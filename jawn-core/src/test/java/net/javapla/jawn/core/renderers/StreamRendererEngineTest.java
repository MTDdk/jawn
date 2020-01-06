package net.javapla.jawn.core.renderers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.internal.renderers.StreamRendererEngine;

public class StreamRendererEngineTest {

    static StreamRendererEngine engine;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        engine = new StreamRendererEngine();
        
    }
    
    private Context context;
    private Context.Response response;
    
    @Before
    public void setUp() throws Exception {
        response = mock(Context.Response.class);
        
        context = mock(Context.class);
        when(context.resp()).thenReturn(response);
    }

    @Test
    public void invokeByteArray() throws Exception {
        byte[] o = "testing string".getBytes();
        
        engine.invoke(context,o);
        
        verify(response, never()).send(anyString());
        verify(response, times(1)).send(eq(o));
        verify(response, never()).send(any(ByteBuffer.class));
        verify(response, never()).send(any(CharBuffer.class));
        verify(response, never()).send(any(InputStream.class));
    }

    @Test
    public void invokeInputstream() throws Exception {
        Object o = new ByteArrayInputStream("testing string".getBytes());
        
        engine.invoke(context,o);
        
        verify(response, never()).send(anyString());
        verify(response, never()).send(any(byte[].class));
        verify(response, never()).send(any(ByteBuffer.class));
        verify(response, never()).send(any(CharBuffer.class));
        verify(response, times(1)).send(any(InputStream.class));
    }
    
    @Test
    public void invokeFile() throws Exception {
        Object o = Paths.get("src", "test", "resources", "webapp", "favicon.ico").toFile();
        
        engine.invoke(context,o);
        
        verify(response, never()).send(anyString());
        verify(response, never()).send(any(byte[].class));
        verify(response, never()).send(any(ByteBuffer.class));
        verify(response, never()).send(any(CharBuffer.class));
        verify(response, times(1)).send(any(InputStream.class));
    }
}
