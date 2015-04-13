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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * Registration facility for {@link ControllerMetaData}.
 *
 * @author Igor Polevoy
 */
@Deprecated
public class ControllerRegistry {

    /**
     * key - controller class name, value ControllerMetaData.
     */
//    private Map<String, ControllerMetaData> metaDataMap = new HashMap<String, ControllerMetaData>();
//    private List<FilterList> globalFilterLists = new ArrayList<FilterList>();
    private Injector injector;
    
    private List<AbstractModule> modules;
    

    // these are not full package names, just partial package names between "app.controllers"
    // and simple name of controller class
//    private List<String> controllerPackages;

    private final Object token = new Object();

    private boolean filtersInjected = false;


    protected ControllerRegistry(/*ServletContext context*/) {
//        controllerPackages = ControllerPackageLocator.locateControllerPackages(context);
        modules = new ArrayList<>();
    }


    /**
     * Returns controller metadata for a class.
     *
     * @param controllerClass controller class.
     * @return controller metadata for a controller class.
     */
    /*protected ControllerMetaData getMetaData(Class<? extends AppController> controllerClass) {
        if (metaDataMap.get(controllerClass.getName()) == null) {
            metaDataMap.put(controllerClass.getName(), new ControllerMetaData());
        }
        return metaDataMap.get(controllerClass.getName());
    }*/



//    protected List<FilterList> getGlobalFilterLists() {
//        return Collections.unmodifiableList(globalFilterLists);
//    }

    protected void setInjector(Injector injector) {
        this.injector = injector;
    }
    
    protected void addModules(AbstractModule... modules) {
        for (AbstractModule module : modules) {
            this.modules.add(module);
        }
    }
    protected List<AbstractModule> getModules() {
        return modules;
    }

    protected Injector getInjector() {
        return injector;
    }


    protected void injectFilters() {
        
        if (!filtersInjected) {
            synchronized (token) {
                if (injector != null) {
                    //inject global filters:
                    /*for (FilterList filterList : globalFilterLists) {
                        List<ControllerFilter> filters = filterList.getFilters();
                        for (ControllerFilter controllerFilter : filters) {
                            injector.injectMembers(controllerFilter);
                        }
                    }*/
                    //inject specific controller filters:
                    /*for (String key : metaDataMap.keySet()) {
                        metaDataMap.get(key).injectFilters(injector);
                    }*/
                }
                filtersInjected = true;
            }
        }
    }

    @Deprecated
    protected List<String> getControllerPackages() {
        return null;//controllerPackages;
    }

    // instance contains a list of filters and corresponding  list of controllers for which these filters
    // need to be excluded.
    /*static class FilterList{
        private final List<ControllerFilter> filters;
        private final List<Class<? extends AppController>> excludedControllers;

        private FilterList(List<ControllerFilter> filters, List<Class<? extends AppController>> excludedControllers) {
            this.filters = filters;
            this.excludedControllers = excludedControllers;
        }

        private FilterList(List<ControllerFilter> filters) {
            this(filters, Collections.<Class<? extends AppController>>emptyList());
        }

        public List<ControllerFilter> getFilters() {
            return Collections.unmodifiableList(filters);
        }

        public boolean excludesController(AppController controller) {

            for (Class<? extends AppController> clazz : excludedControllers) {
                //must use string here, because when controller re-compiles, class instance is different
                if(clazz.getName().equals(controller.getClass().getName()))
                    return true;
            }
            return false;
        }
    }*/
}
