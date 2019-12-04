package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Context.Response;

public class DateUtilTest {

    @Test
    public void toDateString() {
        assertThat(DateUtil.toDateString(Instant.ofEpochMilli(1550581753347L))).isEqualTo("Tue, 19 Feb 2019 13:09:13 GMT");
    }

    @Test
    public void fromDateString() {
        assertThat(DateUtil.fromDateString("Tue, 19 Feb 2019 13:09:13 GMT").getEpochSecond()).isEqualTo(1550581753L);
    }
    
    @Test
    public void adaddDateHeaderIfRequireddDateHeader() {
        Response response = mock(Context.Response.class);
        when(response.header(eq("Date")))
            .thenReturn(Optional.empty()) // first: no date
            .thenReturn(Optional.of("some date")); // second: has been set
        
        
        DateUtil.addDateHeaderIfRequired(response);
        verify(response, times(1)).header(eq("Date"), anyString());
        
        // a date should NOT have been set a second time
        DateUtil.addDateHeaderIfRequired(response);
        verify(response, times(1)).header(eq("Date"), anyString());
    }
}
