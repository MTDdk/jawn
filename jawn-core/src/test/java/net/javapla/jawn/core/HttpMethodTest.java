package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import java.util.function.Supplier;

import org.junit.Test;

import net.javapla.jawn.core.util.MultiList;

public class HttpMethodTest {

    @Test
    public void ajaxForDelete() {
        HttpMethod method = HttpMethod.getMethod("POST", () -> new MultiList<String>().put("_method", "DELETE"));
        assertThat(method).isEqualTo(HttpMethod.DELETE);
    }
    
    @Test
    public void ajaxForPut() {
        HttpMethod method = HttpMethod.getMethod("POST", () -> new MultiList<String>().put("_method", "PUT"));
        assertThat(method).isEqualTo(HttpMethod.PUT);
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
