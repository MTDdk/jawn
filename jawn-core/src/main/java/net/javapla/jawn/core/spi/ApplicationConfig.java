package net.javapla.jawn.core.spi;

import com.google.inject.Binder;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.parsers.ParserEngine;
import net.javapla.jawn.core.renderers.RendererEngine;

public interface ApplicationConfig {

    Binder binder();

    Modes mode();
    
    Config configuration();

    void onStartup(Runnable task);

    void onShutdown(Runnable task);

    void parser(ParserEngine parser);

    void renderer(RendererEngine renderer);

}
