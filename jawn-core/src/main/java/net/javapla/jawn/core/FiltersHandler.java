package net.javapla.jawn.core;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Injector;

import net.javapla.jawn.core.api.Filter;
import net.javapla.jawn.core.api.Filters;
import net.javapla.jawn.core.reflection.ControllerLocator;
import net.javapla.jawn.core.util.PropertiesConstants;

/**
 * FilterHandler
 * 
 * @author MTD
 */
public class FiltersHandler implements Filters {

    private final ArrayList<FilterBuilder<? extends Filter>> builders;
    
    private volatile boolean isInitialised = false;
    
    public FiltersHandler() {
        this.builders = new ArrayList<>();
    }
    
    @Override
    public synchronized FilterBuilder<Filter> add(Filter filter) {
        if (isInitialised) throw new RuntimeException(this.getClass().getSimpleName() + " already initialised");
        
        FilterBuilder<Filter> bob = new FilterBuilder<>(filter);
        builders.add(bob);
        
        return bob;
    }
    
    @Override
    public synchronized FilterBuilder<Filter> add(Class<? extends Filter> filter) {
        if (isInitialised) throw new RuntimeException(this.getClass().getSimpleName() + " already initialised");
        
        FilterBuilder<Filter> bob = new FilterBuilder<>(filter);
        builders.add(bob);
        
        return bob;
    }
    
    @Override
    public synchronized void initialiseFilters(Injector injector) {
        isInitialised = true;
        
        builders.stream().forEach(builder -> 
            builder.injectOrInitialise(injector)
        );
    }
    
    
    /**
     * @deprecated See FilterBuilder#forActions
     */
    @Deprecated
    public List<Filter> compileFilters(Class<? extends Controller> controller, String action) {
        ArrayList<Filter> list = new ArrayList<>();
        /*for (FilterBuilder<? extends Filter> bob : builders) {
            Filter filter = bob.get(controller, action);
            if (filter != null)
                list.add(filter);
        }*/
        
        return list;
    }
    public List<Filter> compileFilters(Class<? extends Controller> controller) {
        ArrayList<Filter> list = new ArrayList<>();
        for (FilterBuilder<? extends Filter> bob : builders) {
            Filter filter = bob.get(controller);
            if (filter != null)
                list.add(filter);
        }
        
        return list;
    }
    
    public List<Filter> compileGlobalFilters() {
        ArrayList<Filter> list = new ArrayList<>();
        for (FilterBuilder<? extends Filter> bob : builders) {
            Filter filter = bob.get(null);
            if (filter != null)
                list.add(filter);
        }
        
        return list;
    }
    

    public static class FilterBuilder<T extends Filter> {
        Filter filter;
        Class<? extends Filter> filterClass;
        Class<? extends Controller>[] controllers;
        
        
        public FilterBuilder(T filter) {
            this.filter = filter;
        }
        
        public FilterBuilder(Class<? extends Filter> filter) {
            filterClass = filter;
        }
        
        @SafeVarargs
        public final FilterBuilder<T> to( Class<? extends Controller>... classes) {
            this.controllers = classes;
            return this;
        }
        
        //@SuppressWarnings("unchecked")
        public final FilterBuilder<T> toPackage( String packageName ) {
            if (!packageName.startsWith(PropertiesConstants.CONTROLLER_PACKAGE))
                packageName = PropertiesConstants.CONTROLLER_PACKAGE + "." + packageName;
            this.controllers = new ControllerLocator(packageName).controllersAsArray();
            return this;
        }
        
        boolean isGlobal() {
            return controllers == null;
        }
        
        void injectOrInitialise(Injector injector) {
            if (filter == null) {
                filter = injector.getInstance(filterClass);
            } else {
                injector.injectMembers(filter);
            }
        }
        
        Filter get(Class<? extends Controller> controller) {
            if (controllers == null) return filter; // the filter it defined globally
            if (controller == null) return null; // we are looking for a global filter, but if controllers != null the filter is not global
            
            return getFilter(controller);
        }
        
        Filter getFilter(Class<? extends Controller> controller) {
            for (Class<? extends Controller> con : controllers) {
                if (con.getName().equals(controller.getName())) {
                    return filter;
                }
            }
            return null;
        }
    }
}
