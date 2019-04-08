package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class UpTest {

    @Test
    public void exceptionWithStatusAndCause() {
        Exception cause = new IllegalArgumentException();
        Up err = new Up(Status.BAD_REQUEST, cause);
        
        assertThat(err.statusCode()).isEqualTo(Status.BAD_REQUEST.value());
        assertThat(err.getCause()).isEqualTo(cause);
    }

}
