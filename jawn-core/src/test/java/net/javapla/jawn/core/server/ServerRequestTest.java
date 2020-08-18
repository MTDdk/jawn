package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.util.MultiList;

public class ServerRequestTest {

    @Test
    public void header() {
        ServerRequest req = mock(ServerRequest.class);
        when(req.header(anyString())).thenCallRealMethod();
        when(req.headers()).thenReturn(new MultiList<String>().put("headername", "headervalue", "value2").put("keysomething", "cookie"));
        
        
        Value header = req.header("headername");
        assertThat(header.isPresent()).isTrue();
        assertThat(header.value()).isEqualTo("headervalue"); // only picks the first
        assertThat(req.headers().list("headername")).hasSize(2); // headervalue, value2
    }

    @Test
    public void headers() {
        ServerRequest req = mock(ServerRequest.class);
        when(req.headers(anyString())).thenCallRealMethod();
        when(req.headers()).thenReturn(new MultiList<String>().put("headername", "headervalue", "value2").put("keysomething", "cookie"));
        
        assertThat(req.headers("headername")).containsExactly("headervalue", "value2");
        assertThat(req.headers("nothing")).isEmpty();
        assertThat(req.headers("keysomething")).isNotEmpty();
    }
    
    @Test
    public void queryParams() {
        ServerRequest req = mock(ServerRequest.class);
        when(req.queryParams(anyString())).thenCallRealMethod();
        when(req.queryParams()).thenReturn(new MultiList<String>().put("headername", "headervalue", "value2").put("keysomething", "cookie"));
        
        assertThat(req.queryParams("headername")).containsExactly("headervalue", "value2");
        assertThat(req.queryParams("nothing")).isEmpty();
        assertThat(req.queryParams("keysomething")).isNotEmpty();
    }
    
    @Test
    public void formData() {
        ServerRequest req = mock(ServerRequest.class);
        when(req.formData(anyString())).thenCallRealMethod();
        
        FormItem password = mock(FormItem.class);
        when(req.formData()).thenReturn(new MultiList<FormItem>().put("name", mock(FormItem.class)).put("password", password));
        when(password.name()).thenReturn("password");
        when(password.value()).thenReturn(Optional.of("123"));
        
        assertThat(req.formData("nothing").isPresent()).isFalse();
        assertThat(req.formData("name").isPresent()).isTrue();
        assertThat(req.formData("password").get().value().get()).isEqualTo("123");
    }
}
