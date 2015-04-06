/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package net.javapla.jawn;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This is an abstract class designed to be overridden in the application. The name for a subclass is:
 * <code>app.config.AppBootstrap</code>. This class is called by the framework during initialization.
 *
 * @see AbstractDBConfig
 * @see AbstractControllerConfig
 *
 * @author Igor Polevoy
 */
public abstract class Bootstrap extends AppConfig {
    
    private Injector applicationInjector;
    private AbstractModule[] modules;

    

    /**
     * Called when application is destroyed (un-deployed).
     * Override to catch event.
     * 
     * @param context app context instance
     */
    public void destroy(AppContext context){}

    
    /**
     * 
     * @param modules One or more modules to be included
     */
    public void putModules(AbstractModule... modules){
        this.modules = modules;
//        applicationInjector = Guice.createInjector(modules);
//        Context.getControllerRegistry().setInjector(applicationInjector);
    }
    
    @Override
    public void completeInit(ControllerRegistry registry) {
//        registry.setInjector(applicationInjector);
        registry.addModules(modules);
    }
    
    
    
}
