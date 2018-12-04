package net.javapla.jawn.core.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.AbstractModule;

import net.javapla.jawn.core.Env;

public class ApplicationConfig {

    private final List<AbstractModule> modules;
    private final Env env;
    
    public ApplicationConfig(Env env) {
        modules = new ArrayList<>();
        this.env = env;
    }
    
    public void registerModules(AbstractModule... modules) {
        Collections.addAll(this.modules, modules);
    }
    
    public List<AbstractModule> getRegisteredModules() {
        //return modules.stream().toArray(AbstractModule[]::new);
        return this.modules;
    }
    
    public Env env() {
        return env;
    }
}
