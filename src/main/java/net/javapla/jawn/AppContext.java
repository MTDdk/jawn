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

import javax.servlet.ServletContext;

/**
 * Instance of this class can hold application-specific objects for duration of application life span.
 *
 * @author Igor Polevoy
 */
/**
 * Wrapping the ServletContext as there is no need for having several contexts.
 * <p>
 * Making sure only needed options for manipulation is exposed
 *  
 * @author MTD
 */
public class AppContext {
    
    public static final String PREFIX = "net.javapla.jawn.";
    public static final String ENCODING = PREFIX + "encoding";
    public static final String LANGUAGES = PREFIX + "supportedLanguages";
    public static final String DATABASE_SPECS = PREFIX + "databaseSpecs";

    private final ServletContext context;

    // README this may not be the best of ways to do it
    AppContext(ServletContext context) {
        this.context = context;
    }

    /**
     * Retrieves object by name
     *
     * @param name name of object
     * @return   object by name;
     */
    public Object get(String name){
        return context.getAttribute(PREFIX + name);
    }
    
    public String getAsString(String name) {
        return (String) get(name);
    }
    
    /**
     * Retrieves object by name. Convenience generic method. 
     *
     * @param name name of object
     * @param type type requested.
     * @return object by name
     */
    public <T>  T get(String name, Class<T> type){
        Object o = get(name);
        return o == null? null : type.cast(o);
    }

    /**
     * Sets an application - wide object by name.
     *
     * @param name name of object
     * @param object - instance. 
     */
    public void set(String name, Object object){
        context.setAttribute(PREFIX + name, object);
    }
    
    
    /**
     * 
     * @return
     * @author MTD
     */
    public String getEncoding() {
        return getAsString(ENCODING);
    }
    public void setEncoding(String enc) {
        set(ENCODING, enc);
    }
    
    /**
     * First element is the default language
     * @return
     */
    public String[] getSupportedLanguages() {
        return get(LANGUAGES, String[].class);
    }
    
    /**
     * First element is the default language
     */
    public void setSupportedLanguages(String... languages) {
        set(LANGUAGES, languages);
    }
    
}
