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
package net.javapla.jawn.core;


import net.javapla.jawn.core.util.StreamUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Always loads a class from a file. No caching of any kind.
 * Needs to be re-instantiated each time the same class needs to be reloaded.
 * 
 * Dynamic class reloading is a bit more challenging. 
 * Java's builtin Class loaders always checks if a class is already loaded before loading it. 
 * Reloading the class is therefore not possible using Java's builtin class loaders. 
 * To reload a class you will have to implement your own ClassLoader subclass.
 * http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html#dynamicreloading
 */
class DynamicClassLoader extends ClassLoader {

    private static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);

    DynamicClassLoader(ClassLoader parent){
        super(parent);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {

        try{
            if (name.startsWith(this.getClass().getPackage().getName())) {
                return loadByParent(name);
            }

            // only do reloading on Controllers
            //README this currently also looks for RouteConfig - might need to be removed
            if(name.endsWith("Controller") || name.contains("Controller$")
                    /*|| name.equals(Configuration.getRouteConfigClassName())*/){

                String pathToClassFile = name.replace('.', '/') + ".class";

                byte[] classBytes = StreamUtil.bytes(getResourceAsStream(pathToClassFile));
                Class<?> daClass = defineClass(name, classBytes, 0, classBytes.length);

                logger.debug("Loaded class: " + name);
                return daClass;
            }else{
                return loadByParent(name);
            }
        }
        catch(Exception e){
            logger.debug("Failed to dynamically load class: " + name + ". Loading by parent class loader.");
            return loadByParent(name);
        }
    }

    private Class<?> loadByParent(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }

}
