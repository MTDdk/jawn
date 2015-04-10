package net.javapla.jawn;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * FilterBuilder
 * 
 * @author MTD
 *
 */
public class Filters {

    private final List<FilterBuilder> builders;
    
    public Filters() {
        this.builders = new ArrayList<>();
    }
    
    public FilterBuilder add(Filter filter) {
        FilterBuilder bob = new FilterBuilder(filter);
        builders.add(bob);
        return bob;
    }
    
    public List<Filter> compileFilters(Class<? extends AppController> controller, String action) {
        List<Filter> list = new ArrayList<>();
        for (FilterBuilder bob : builders) {
            Filter filter = bob.get(controller, action);
            if (filter != null)
                list.add(filter);
        }
        return list;
    }
    public List<Filter> compileFilters(Class<? extends AppController> controller) {
        List<Filter> list = new ArrayList<>();
        for (FilterBuilder bob : builders) {
            Filter filter = bob.get(controller);
            if (filter != null)
                list.add(filter);
        }
        return list;
    }
    
    
    public class FilterBuilder {
        Filter filter;
        Class<? extends AppController>[] controllers;
        String[] actionNames;
        
        public FilterBuilder(Filter filter) {
            this.filter = filter;
        }
        
        @SafeVarargs
        public final FilterBuilder to( Class<? extends AppController>... classes) {
            this.controllers = classes;
            if (actionNames != null) ensureControllersContainsActions();
            return this;
        }
        
        public FilterBuilder forActions(String... actionNames) {
            this.actionNames = actionNames;
            if (this.controllers != null) {
                ensureControllersContainsActions();
            }
            return this;
        }
        
        private void ensureControllersContainsActions() {
            for (Class<? extends AppController> controller : controllers) {
                if (!doesControllerHaveActions(controller, actionNames))
                    throw new IllegalArgumentException("Controller does not have an action resembling the provided - " + controller.getSimpleName());
            }
        }
        private boolean doesControllerHaveActions(Class<? extends AppController> controller, String[] actions) {
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
        
        public Filter get(Class<? extends AppController> controller, String action) {
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
        }
        public Filter get(Class<? extends AppController> controller) {
            if (actionNames != null) return null;
            if (controllers == null) return filter; // the filter it defined globally
            
            return getFilter(controller);
        }
        
        private Filter getFilter(Class<? extends AppController> controller) {
            for (Class<? extends AppController> con : controllers) {
                if (con.getName().equals(controller.getName())) {
                    return filter;
                }
            }
            return null;
        }
    }
}
