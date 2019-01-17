package net.javapla.jawn.core.spi;

import com.google.inject.Binder;

import net.javapla.jawn.core.util.Modes;

public interface ApplicationConfig {

    Binder binder();

    Modes mode();

    void onStartup(Runnable task);

    void onShutdown(Runnable task);

}
