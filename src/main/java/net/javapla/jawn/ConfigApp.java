package net.javapla.jawn;

import com.google.inject.AbstractModule;

public class ConfigApp {
    private AbstractModule[] modules;
    private String[] languages;

    public void registerModules(AbstractModule... modules) {
        this.modules = modules;
    }
    
    AbstractModule[] getRegisteredModules() {
        return modules;
    }
    
    public void setSupportedLanguages(String... languages) {
        this.languages = languages;
    }
    
    String[] getSupportedLanguages() {
        return this.languages;
    }
}
