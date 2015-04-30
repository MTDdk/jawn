package net.javapla.jawn.core;

import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.parsers.ParserEngineManager;
import net.javapla.jawn.core.parsers.ParserEngineManagerImpl;
import net.javapla.jawn.core.parsers.XmlMapperProvider;
import net.javapla.jawn.core.reflection.ControllerActionInvoker;
import net.javapla.jawn.core.templates.TemplateEngineOrchestrator;
import net.javapla.jawn.core.templates.TemplateEngineOrchestratorImpl;
import net.javapla.jawn.core.templates.config.ConfigurationReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class CoreModule extends AbstractModule {
    
    @Override
    protected void configure() {
        
        // Marshallers
        bind(ObjectMapper.class).toProvider(JsonMapperProvider.class).in(Singleton.class);
        bind(XmlMapper.class).toProvider(XmlMapperProvider.class).in(Singleton.class);
        
        bind(ConfigurationReader.class).in(Singleton.class);
        
        bind(TemplateEngineOrchestrator.class).to(TemplateEngineOrchestratorImpl.class).in(Singleton.class);
        bind(ParserEngineManager.class).to(ParserEngineManagerImpl.class).in(Singleton.class);
        bind(ResponseRunner.class).in(Singleton.class);
        
        bind(ControllerActionInvoker.class).in(Singleton.class);
        
        bind(FrameworkEngine.class).in(Singleton.class);
    }

    
}
