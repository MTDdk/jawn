package net.javapla.jawn.core;

import net.javapla.jawn.core.util.Constants;

import com.google.inject.AbstractModule;

public class ApplicationConfig {
    private AbstractModule[] modules;
    private String[] languages;
    private String encoding = Constants.DEFAULT_ENCODING;
    
    public void registerModules(AbstractModule... modules) {
        this.modules = modules;
    }
    
    public AbstractModule[] getRegisteredModules() {
        return modules;
    }
    
    public void setSupportedLanguages(String... languages) {
        this.languages = languages;
    }
    
    public String[] getSupportedLanguages() {
        return this.languages;
    }
    
    public void setCharacterEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getCharacterEncoding() {
        return encoding;
    }
}
