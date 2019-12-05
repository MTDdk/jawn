package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Test;

import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.util.MultiList;

public class HttpMethodTest {

    @Test
    public void ajaxForDelete() {
        assertThat(HttpMethod.getMethod("POST", () -> new MultiList<String>().put("_method", "DELETE"))).isEqualTo(HttpMethod.DELETE);
        assertThat(HttpMethod.getMethod("POST", () -> new MultiList<String>().put("_method", "DEL"))).isEqualTo(HttpMethod.DELETE);
    }
    
    @Test
    public void ajaxForPut() {
        assertThat(HttpMethod.getMethod("POST", () -> new MultiList<String>().put("_method", "PUT"))).isEqualTo(HttpMethod.PUT);
    }
    
    @Test
    public void ajaxForInvalid() {
        assertThat(HttpMethod.getMethod("POST", () -> new MultiList<String>().put("_method", "INVALID"))).isEqualTo(HttpMethod.POST);
    }
    
    @Test
    public void formItemForDelete() {
        FormItem item = mock(FormItem.class);
        when(item.value()).thenReturn(Optional.of("DELETE"));
        
        assertThat(HttpMethod.getMethod("POST", () -> new MultiList<FormItem>().put("_method", item))).isEqualTo(HttpMethod.DELETE);
    }
    
    @Test
    public void formItemForPut() {
        FormItem item = mock(FormItem.class);
        when(item.value()).thenReturn(Optional.of("PUT"));
        
        assertThat(HttpMethod.getMethod("POST", () -> new MultiList<FormItem>().put("_method", item))).isEqualTo(HttpMethod.PUT);
    }

    @Test
    public void getRequestMethod() {
        Supplier<CharSequence> s = () -> null;
        assertThat(HttpMethod._getMethod("GET", s)).isEqualTo(HttpMethod.GET);
        assertThat(HttpMethod._getMethod("GE", s)).isEqualTo(HttpMethod.GET);
        assertThat(HttpMethod._getMethod("G", s)).isEqualTo(HttpMethod.GET);
        
        assertThat(HttpMethod._getMethod("DELETE", s)).isEqualTo(HttpMethod.DELETE);
        assertThat(HttpMethod._getMethod("DEL", s)).isEqualTo(HttpMethod.DELETE);
        
        assertThat(HttpMethod._getMethod("HEAD", s)).isEqualTo(HttpMethod.HEAD);
        assertThat(HttpMethod._getMethod("HE", s)).isEqualTo(HttpMethod.HEAD);
        
        assertThat(HttpMethod._getMethod("OPTIONS", s)).isEqualTo(HttpMethod.OPTIONS);
        assertThat(HttpMethod._getMethod("OPT", s)).isEqualTo(HttpMethod.OPTIONS);
        
        // if starting with P and not directly stated as POST or PUT, then always assume POST
        assertThat(HttpMethod._getMethod("POST", s)).isEqualTo(HttpMethod.POST);
        assertThat(HttpMethod._getMethod("PUT", s)).isEqualTo(HttpMethod.PUT);
        assertThat(HttpMethod._getMethod("PLAIN", s)).isEqualTo(HttpMethod.POST);
        assertThat(HttpMethod._getMethod("PEACE", s)).isEqualTo(HttpMethod.POST);
        
        assertThrows(IllegalArgumentException.class, () -> HttpMethod._getMethod("WAT?", s));
    }
    
    @Test
    public void supplier_should_execute() {
        boolean[] b = new boolean[1];
        Supplier<MultiList<? extends CharSequence>> supplier = () -> {
            b[0] = true;
            return new MultiList<String>().put(HttpMethod.AJAX_METHOD_PARAMETER, "PUT");
        };
        HttpMethod method = HttpMethod.getMethod("POST", supplier);
        assertThat(method).isEqualTo(HttpMethod.PUT);
        assertThat(b[0]).isTrue();
    }
    
    @Test
    public void supplier_should_not_bePrematurelyExecuted() {
        boolean[] b = new boolean[1];
        Supplier<MultiList<? extends CharSequence>> supplier = () -> {
            b[0] = true;
            return new MultiList<String>().put(HttpMethod.AJAX_METHOD_PARAMETER, "DELETE");
        };
        HttpMethod method = HttpMethod.getMethod("PUT", supplier);
        assertThat(method).isEqualTo(HttpMethod.PUT);
        assertThat(b[0]).isFalse();
    }

}
