package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class ErrTest {

    @Test
    public void exceptionWithStatusAndCause() {
        Exception cause = new IllegalArgumentException();
        Err err = new Err(Status.BAD_REQUEST, cause);
        
        assertThat(err.statusCode()).isEqualTo(Status.BAD_REQUEST.value());
        assertThat(err.getCause()).isEqualTo(cause);
    }

}
