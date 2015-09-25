package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.javapla.jawn.core.util.Constants;

import com.google.inject.AbstractModule;

public class ApplicationConfig {
    private List<AbstractModule> modules;
    private String[] languages;
    private String encoding = Constants.DEFAULT_ENCODING;
    private boolean useDefaultRouting = true;
    
    public ApplicationConfig() {
        modules = new ArrayList<>();
    }
    
    public void registerModules(AbstractModule... modules) {
        Collections.addAll(this.modules, modules);
    }
    
    public List<AbstractModule> getRegisteredModules() {
        //return modules.stream().toArray(AbstractModule[]::new);
        return this.modules;
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
    
    public void useDefaultRouting(boolean useDefaultRouting) {
        this.useDefaultRouting = useDefaultRouting;
    }
    public boolean useDefaultRouting() {
        return useDefaultRouting;
    }
}
