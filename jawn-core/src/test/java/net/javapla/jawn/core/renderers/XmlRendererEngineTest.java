package net.javapla.jawn.core.renderers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.internal.renderers.XmlRendererEngine;
import net.javapla.jawn.core.parsers.XmlMapperProvider;
import net.javapla.jawn.core.renderers.JsonRendererEngineTest.T;

public class XmlRendererEngineTest {
    
    static XmlRendererEngine engine;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        engine = new XmlRendererEngine(new XmlMapperProvider().get());
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
        byte[] o = "<T><key>testing string</key></T>".getBytes();
        
        engine.invoke(context, o);
        
        verify(response, never()).send(anyString());
        verify(response, times(1)).send(eq(o));
        verify(response, never()).send(any(ByteBuffer.class));
        verify(response, never()).send(any(CharBuffer.class));
        verify(response, never()).send(any(InputStream.class));
    }
    
    @Test
    public void invokeString() throws Exception {
        String o = "<T><key>testing string</key></T>";
        
        when(response.charset()).thenReturn(StandardCharsets.UTF_16);
        
        engine.invoke(context, o);
        
        verify(response, never()).send(anyString());
        verify(response, times(1)).send(eq(o.getBytes(StandardCharsets.UTF_16)));
        verify(response, never()).send(any(ByteBuffer.class));
        verify(response, never()).send(any(CharBuffer.class));
        verify(response, never()).send(any(InputStream.class));
    }
    
    @Test
    public void invokeObject() throws Exception {
        T o = new T("testing string");
        
        engine.invoke(context, o);
        
        verify(response, never()).send(anyString());
        verify(response, times(1)).send(eq("<T><key>testing string</key></T>".getBytes()));
        verify(response, never()).send(any(ByteBuffer.class));
        verify(response, never()).send(any(CharBuffer.class));
        verify(response, never()).send(any(InputStream.class));
    }
}
