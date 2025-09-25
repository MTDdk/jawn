package net.javapla.jawn.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;

class SessionStoreTest {
    
    static String signedValue = "QTEwRjAvcXNibnZ5YVgwMkdNeFdzMG1pU2ozKzhMdXk0aUwxZXkxeUxXNHxzb21la2V5PXB1dHZhbHVl";
    static String sessionName = "x-random-header";

    @Test
    void signed() {
        
        Context context = mock(Context.class);

        SessionToken token = SessionToken.headerToken(sessionName);
        SessionStore store = SessionStore.signed("secret", token);
        
        Context.Response response = mock(Context.Response.class);
        when(context.resp()).thenReturn(response);
        
        Session session = store.newSession(context);
        session.put("somekey", "putvalue");
        //store.touchSession(context, session); // gets called in Session.put
        
        verify(response).header(eq(sessionName), eq(signedValue));
        
        store.deleteSession(context, session);
        
    }
    
    @Test
    void unsigned() {
        Context context = mock(Context.class);
        
        SessionToken token = SessionToken.headerToken(sessionName);
        SessionStore store = SessionStore.signed("secret", token);
        
        Context.Request request = mock(Context.Request.class);
        when(request.header(eq(sessionName))).thenReturn(Value.of(signedValue));
        when(context.req()).thenReturn(request);
        
        Session session = store.findSession(context);
        Map<String, String> data = session.data();
        assertEquals(1, data.size());
        assertEquals("putvalue", session.get("somekey").value());
    }
    
    @Test
    //@Ignore
    void decode() {
        Context context = mock(Context.class);

        SessionToken token = SessionToken.headerToken(sessionName);
        SessionStore store = SessionStore.signed("obscure-user-data", token);
        
        Context.Request request = mock(Context.Request.class);
        when(request.header(eq(sessionName))).thenReturn(Value.of("TlZjUG1KY0pPWC83L28rMlRROUJCRjV2K0VvYTAxaGQyNnJwR1BUeEYxZ3xjYXRlZ29yeT1jbGlwJnVzZXI9Mg"));
        when(context.req()).thenReturn(request);
        
        
        /*Context.Response response = mock(Context.Response.class);
        when(context.resp()).thenReturn(response);
        when(response.header(anyString(), anyString())).thenAnswer(invocation -> {
            System.out.println((String)invocation.getArgument(1));
            return response;
        });
        Session s = store.newSession(context);
        s.put("category", "clip");
        s.put("user", "2");*/
        
        
        Session session = store.findSession(context);
        Map<String, String> data = session.data();
        
        assertEquals(2, data.size());
        assertEquals("clip", data.get("category"));
        assertEquals("2", data.get("user"));
    }

}
