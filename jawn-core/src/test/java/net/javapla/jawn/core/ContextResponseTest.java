package net.javapla.jawn.core;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import net.javapla.jawn.core.Context.Response;

public class ContextResponseTest {

    @Test
    public void charsetString_should_findCorrectEncoding() {
        Response response = mock(Context.Response.class);
        when(response.charset(anyString())).thenCallRealMethod();
        
        response.charset("iso-8859-1");
        response.charset("iso_8859_1");
        response.charset("ISO_8859-1");
        
        verify(response, times(3)).charset(eq(StandardCharsets.ISO_8859_1));
    }
    
    @Test
    public void charsetString_should_not_throwWhenCrazyEncoding() {
        Response response = mock(Context.Response.class);
        when(response.charset(anyString())).thenCallRealMethod();
        
        response.charset("uTf-127");
        response.charset("niso-1986");
    }
    
    @Test
    public void cookieNameValue_should_buildACookie() {
        Response response = mock(Context.Response.class);
        when(response.cookie(anyString(), anyString())).thenCallRealMethod();
        
        response.cookie("cookiename", "cookievalue");
        
        verify(response, times(1)).cookie(argThat((Cookie c) -> c.name().equals("cookiename") && c.value().equals("cookievalue")));
    }
}
