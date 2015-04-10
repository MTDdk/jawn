package net.javapla.jawn;

import net.javapla.jawn.i18n.Lang;
import net.javapla.jawn.parsers.JsonMapperProvider;
import net.javapla.jawn.parsers.ParserEngineManager;
import net.javapla.jawn.parsers.ParserEngineManagerImpl;
import net.javapla.jawn.parsers.XmlMapperProvider;
import net.javapla.jawn.templates.TemplateEngineManager;
import net.javapla.jawn.templates.TemplateEngineManagerImpl;
import net.javapla.jawn.templates.configuration.ConfigurationReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class CoreModule extends AbstractModule {
    
    private final PropertiesImpl properties;
    
    CoreModule(PropertiesImpl properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        bind(PropertiesImpl.class).toInstance(properties);
        bind(Lang.class).in(Singleton.class);
        
        // Marshallers
        bind(ObjectMapper.class).toProvider(JsonMapperProvider.class).in(Singleton.class);
        bind(XmlMapper.class).toProvider(XmlMapperProvider.class).in(Singleton.class);
        
        bind(ConfigurationReader.class).in(Singleton.class);
        
        bind(TemplateEngineManager.class).to(TemplateEngineManagerImpl.class);
        bind(ParserEngineManager.class).to(ParserEngineManagerImpl.class);
        bind(ResponseRunner.class);
        
        bind(Context.class);
        
        bind(ControllerActionInvoker.class).in(Singleton.class);
        bind(FilterChainEnd.class);
        
//        bind(Router.class).in(Singleton.class);
        
//        bind(Router.class);
        
//        bind(Context.class);
        
//        bind(Request.class);
        
        bind(FrameworkEngine.class).in(Singleton.class);
    }

    
}
