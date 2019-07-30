package net.javapla.jawn.core.server;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class ThrowableCause {

    public static boolean connectionResetByPeer(final Throwable cause) {
        return Optional.ofNullable(cause)
            .filter(IOException.class::isInstance)
            .map(x -> x.getMessage())
            .filter(Objects::nonNull)
            .map(message -> message.toLowerCase().contains("connection reset by peer"))
            .orElse(false);
      }
}
