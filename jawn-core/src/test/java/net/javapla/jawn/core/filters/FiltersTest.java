package net.javapla.jawn.core.filters;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;

import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;

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
        when(request.queryString()).thenReturn(Optional.of("searching"));
        when(context.req()).thenReturn(request);
        
        LogRequestPropertiesFilter filter = new LogRequestPropertiesFilter();
        Result result = filter.before(context, chain);
        assertThat(result.status().value()).isEqualTo(222);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void logRequestTiming() {
        Route.Chain chain = c -> Results.status(223);
        HashMap<String, Object> attributes = new HashMap<>(1);
        
        Request request = mock(Context.Request.class);
        Response response = mock(Context.Response.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(context.resp()).thenReturn(response);
        
        doAnswer(AdditionalAnswers.<String, Object>answerVoid( (p1, p2) -> attributes.put(p1, p2))).when(context).attribute(anyString(), any(Object.class));
        when(context.attribute(anyString(), any(Class.class))).then( AdditionalAnswers.answer( (p1, p2) -> Optional.of(attributes.get(p1))) );
        
        
        LogRequestTimingFilter filter = new LogRequestTimingFilter();
        Result result = filter.before(context, chain);
        assertThat(result.status().value()).isEqualTo(223);
        
        Result after = filter.after(context, result);
        assertThat(after.toString()).isEqualTo(result.toString());
        verify(response).header(eq(LogRequestTimingFilter.X_REQUEST_PROCESSING_TIME), anyString());
    }
    
    @Test
    public void dateHeaderBefore() {
        Route.Chain chain = c -> Results.status(201);
        
        Response response = mock(Context.Response.class);
        Context context = mock(Context.class);
        when(context.resp()).thenReturn(response);

        
        DateHeaderBefore filter = new DateHeaderBefore();
        Result result = filter.before(context, chain);
        assertThat(result.status().value()).isEqualTo(201);
        filter.stop();
        
        
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).header(eq("Date"), captor.capture());
    }
}
