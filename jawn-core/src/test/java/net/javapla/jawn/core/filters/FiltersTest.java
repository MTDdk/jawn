package net.javapla.jawn.core.filters;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Context.Request;
import net.javapla.jawn.core.Context.Response;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;

public class FiltersTest {

    @Test
    public void logRequestProperties() {
        Route.Chain chain = c -> Results.status(222);
        
        Context context = mock(Context.class);
        Request request = mock(Context.Request.class);
        when(context.req()).thenReturn(request);
        
        LogRequestPropertiesFilter filter = new LogRequestPropertiesFilter();
        Result result = filter.before(context, chain);
        assertThat(result.status().value()).isEqualTo(222);
    }
    
    @Test
    public void logRequestTiming() {
        Route.Chain chain = c -> Results.status(223);
        
        Request request = mock(Context.Request.class);
        Response response = mock(Context.Response.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(context.resp()).thenReturn(response);
        
        LogRequestTimingFilter filter = new LogRequestTimingFilter();
        Result result = filter.before(context, chain);
        assertThat(result.status().value()).isEqualTo(223);
        
        Result after = filter.after(context, result);
        assertThat(after.toString()).isEqualTo(result.toString());
    }
}
