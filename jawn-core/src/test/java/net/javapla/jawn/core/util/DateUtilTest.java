package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;

import java.time.Instant;

import org.junit.Test;

public class DateUtilTest {


    @Test
    public void toDateString() {
        assertThat(DateUtil.toDateString(Instant.ofEpochMilli(1550581753347L))).isEqualTo("Tue, 19 Feb 2019 13:09:13 GMT");
    }

    @Test
    public void fromDateString() {
        assertThat(DateUtil.fromDateString("Tue, 19 Feb 2019 13:09:13 GMT").getEpochSecond()).isEqualTo(1550581753L);
    }
}
