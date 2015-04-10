package net.javapla.jawn;

import com.google.inject.AbstractModule;

public class ConfigApp {
    private AbstractModule[] modules;

    public void registerModules(AbstractModule... modules) {
        this.modules = modules;
    }
    
    AbstractModule[] getRegisteredModules() {
        return modules;
    }
}
