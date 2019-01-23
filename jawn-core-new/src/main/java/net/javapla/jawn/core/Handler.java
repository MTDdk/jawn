package net.javapla.jawn.core;

@FunctionalInterface
public interface Handler {
    Result handle(Context context);
}