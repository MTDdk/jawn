package net.javapla.jawn;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class Bootstrapper {
    
    final PropertiesImpl properties;
    final List<AbstractModule> userModules;
    Injector injector;

    public Bootstrapper(PropertiesImpl conf, List<AbstractModule> modules) {
        properties = conf;
        userModules = modules;
    }
    
    // TODO factor into constructor
    public synchronized void boot() {
        if (injector != null) throw new RuntimeException(Bootstrapper.class.getSimpleName() + " already initialised");
        
        injector = initInjector();
    }
    
    public Injector getInjector() {
        return injector;
    }

    private Injector initInjector() {
        // this class is a part of the server project
        // configure all the needed dependencies for the server
        // this includes injecting templatemanager
        
        List<AbstractModule> combinedModules = new ArrayList<>();
        combinedModules.add(new CoreModule(properties));
        combinedModules.addAll(userModules);
        
        
        
        return Guice.createInjector(Stage.PRODUCTION, combinedModules);
    }
}
