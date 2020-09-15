package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;


public class SessionStoreTest {
    
    static String signedValue = "QTEwRjAvcXNibnZ5YVgwMkdNeFdzMG1pU2ozKzhMdXk0aUwxZXkxeUxXNHxzb21la2V5PXB1dHZhbHVl";
    static String sessionName = "x-random-header";
    
    @Test
    @Ignore
    public void decode() {
        Context context = mock(Context.class);

        SessionToken token = SessionToken.headerToken(sessionName);
        SessionStore store = SessionStore.signed("award2021", token);
        
        Context.Request request = mock(Context.Request.class);
        when(request.header(eq(sessionName))).thenReturn(Value.of("bkRpdmpKTmdjMXFqYStuWTgvRGxXakUvcmtRZXpQbUxQTUljVjlLSDgyb3xjYXRlZ29yeT1jbGlwJnVzZXI9Mg"));
        when(context.req()).thenReturn(request);
        
        Session session = store.findSession(context);
        Map<String, String> data = session.data();
        System.out.println(data);
    }

    @Test
    public void signed() {
        
        Context context = mock(Context.class);

        SessionToken token = SessionToken.headerToken(sessionName);
        SessionStore store = SessionStore.signed("secret", token);
        
        Context.Response response = mock(Context.Response.class);
        when(context.resp()).thenReturn(response);
        
        Session session = store.newSession(context);
        session.put("somekey", "putvalue");
        //store.touchSession(context, session);
        
        verify(response).header(eq(sessionName), eq(signedValue));
        
        store.deleteSession(context, session);
        
        
        
    }
    
    @Test
    public void unsigned() {
        Context context = mock(Context.class);
        
        SessionToken token = SessionToken.headerToken(sessionName);
        SessionStore store = SessionStore.signed("secret", token);
        
        Context.Request request = mock(Context.Request.class);
        when(request.header(eq(sessionName))).thenReturn(Value.of(signedValue));
        when(context.req()).thenReturn(request);
        
        Session session = store.findSession(context);
        Map<String, String> data = session.data();
        assertThat(data).hasSize(1);
        assertThat(session.get("somekey").value()).isEqualTo("putvalue");
    }

}
