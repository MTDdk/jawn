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
        
        FilterBuilder<Filter> bob = new FilterBuilder<Filter>(filter);
        builders.add(bob);
        
        return bob;
    }
    
    @Override
    public synchronized void initialiseFilters(Injector injector) {
        isInitialised = true;
        
        builders.stream().forEach(builder -> 
            injector.injectMembers(builder.filter)
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
        final T filter;
        Class<? extends Controller>[] controllers;
        //String[] actionNames;
        
        
        public FilterBuilder(T filter) {
            this.filter = filter;
        }
        
        /*@SafeVarargs
        public FilterBuilder(Class<? extends Controller>... classes) {
            this.controllers = classes;
        }*/
        
        
        @SafeVarargs
        public final FilterBuilder<T> to( Class<? extends Controller>... classes) {
            this.controllers = classes;
            //if (actionNames != null) ensureControllersContainsActions();
            return this;
        }
        
        //@SuppressWarnings("unchecked")
        public final FilterBuilder<T> toPackage( String packageName ) {
            if (!packageName.startsWith(PropertiesConstants.CONTROLLER_PACKAGE))
                packageName = PropertiesConstants.CONTROLLER_PACKAGE + "." + packageName;
            this.controllers = new ControllerLocator(packageName).controllersAsArray();
            return this;
        }
        
        /**
         * @deprecated Might not be possible anymore due to the introduction of {@link Jawn#get(String, Class, java.util.function.Consumer)}
         * where action name no longer is saved
         */
        @Deprecated
        public FilterBuilder<T> forActions(String... actionNames) {
            /*this.actionNames = actionNames;
            if (this.controllers != null) {
                ensureControllersContainsActions();
            }*/
            return this;
        }
        
        /*public FilterBuilder<T> with(T filter) {
            this.filter = filter;
            return this;
        }*/
        
        boolean isGlobal() {
            return controllers == null;
        }
        
        /*private void ensureControllersContainsActions() {
            for (Class<? extends Controller> controller : controllers) {
                if (!doesControllerHaveActions(controller, actionNames))
                    throw new IllegalArgumentException("Controller does not have an action resembling the provided - " + controller.getSimpleName());
            }
        }
        private boolean doesControllerHaveActions(Class<? extends Controller> controller, String[] actions) {
            for (Method method : controller.getDeclaredMethods()) {
                if (doesMethodResemble(method, actions)) {
                    return true;
                }
            }
            return false;
        }
        private boolean doesMethodResemble(Method method, String[] actions) {
            for (String action : actions) {
                if (method.getName().equals(action))
                    return true;
            }
            return false;
        }
        
        Filter get(Class<? extends Controller> controller, String action) {
            if (actionNames == null) return null;
            
            Filter f = getFilter(controller);
            if (f != null && action != null) {
                for (String name : actionNames) {
                    if (name.equals(action)) {
                        return filter;
                    }
                }
            }
            return null;
        }*/
        Filter get(Class<? extends Controller> controller) {
            //if (actionNames != null) return null;
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
