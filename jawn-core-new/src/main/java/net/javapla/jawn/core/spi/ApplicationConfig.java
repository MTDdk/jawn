package net.javapla.jawn.core.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.AbstractModule;

public class ApplicationConfig {

    private final List<AbstractModule> modules;
    //private final Config config;
    
    public ApplicationConfig(/*final Config config*/) {
        modules = new ArrayList<>();
        //this.config = config;
    }
    
    public void registerModules(AbstractModule... modules) {
        Collections.addAll(this.modules, modules);
    }
    
    public List<AbstractModule> getRegisteredModules() {
        //return modules.stream().toArray(AbstractModule[]::new);
        return this.modules;
    }
    
    /*public Config config() {
        return config;
    }*/
}
