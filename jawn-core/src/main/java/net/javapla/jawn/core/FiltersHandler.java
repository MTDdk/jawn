package net.javapla.jawn.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import net.javapla.jawn.core.database.DatabaseConnection;
import net.javapla.jawn.core.database.DatabaseConnectionAware;
import net.javapla.jawn.core.security.SecurityFilter;
import net.javapla.jawn.core.spi.Filter;
import net.javapla.jawn.core.spi.Filters;

/**
 * FilterHandler
 * 
 * @author MTD
 */
public class FiltersHandler implements Filters, DatabaseConnectionAware {

    private final List<FilterBuilder<? extends Filter>> builders;
    
    private DatabaseConnection databaseConnection;
//    private SecurityFilterFactory security;
    
    public FiltersHandler() {
        this.builders = new ArrayList<>();
    }
    
//    public FiltersHandler(SecurityFilterFactory security) {
//        this();
//        this.security = security;
//    }
    
    @Override
    public FilterBuilder<Filter> add(Filter filter) {
        FilterBuilder<Filter> bob = new FilterBuilder<Filter>(filter);
        builders.add(bob);
        
        // If the filter is ConnectionAware, then inject the database connection
        if (filter instanceof DatabaseConnectionAware)
            ((DatabaseConnectionAware) filter).setDatabaseConnection(databaseConnection);
        
        return bob;
    }
    
    @Override
    public SecureBuilder secureOnRole(String role) throws IllegalStateException {
//        if (security == null) throw new IllegalStateException("Security is not specified");
//        SecurityFilter filter = security.filter(role);//SecurityFilterFactory.filter(databaseConnection, context, role);
        
        SecureBuilder bob = new SecureBuilder(/*filter*/);
//        builders.add(bob);
//        
//        // If the filter is ConnectionAware, then inject the database connection
//        if (filter instanceof DatabaseConnectionAware)
//            ((DatabaseConnectionAware) filter).setDatabaseConnection(databaseConnection);
        
        return bob;
    }
    
//    private T <T extends FilterBuilder<Filter>> add() {
//        
//    }
    
    public List<Filter> compileFilters(Class<? extends ApplicationController> controller, String action) {
        List<Filter> list = new ArrayList<>();
        for (FilterBuilder<? extends Filter> bob : builders) {
            Filter filter = bob.get(controller, action);
            if (filter != null)
                list.add(filter);
        }
        
        return list;
    }
    public List<Filter> compileFilters(Class<? extends ApplicationController> controller) {
        List<Filter> list = new ArrayList<>();
        for (FilterBuilder<? extends Filter> bob : builders) {
            Filter filter = bob.get(controller);
            if (filter != null)
                list.add(filter);
        }
        
        return list;
    }
    
/* **************************
 * Database connection awares
 * ========================== */
    /*public List<Filter> compileFilters(Class<? extends ApplicationController> controller, String action, DatabaseConnection connection) {
        List<Filter> filters = compileFilters(controller, action);
        if (connection != null) {
            for (Filter filter : filters) {
                if (filter instanceof DatabaseConnectionAware)
                    ((DatabaseConnectionAware) filter).setDatabaseConnection(connection);
            }
        }
        
        return filters;
    }
    public List<Filter> compileFilters(Class<? extends ApplicationController> controller, DatabaseConnection connection) {
        List<Filter> filters = compileFilters(controller);
        if (connection != null) {
            for (Filter filter : filters) {
                if (filter instanceof DatabaseConnectionAware)
                    ((DatabaseConnectionAware) filter).setDatabaseConnection(connection);
            }
        }
        
        return filters;
    }*/
    
    @Override
    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

//    public void setSecurityFilterFactory(SecurityFilterFactory secure) {
//        this.security = secure;
//    }

    public static class FilterBuilder<T extends Filter> {
        T filter;
        Class<? extends ApplicationController>[] controllers;
        String[] actionNames;
        
        
        public FilterBuilder(T filter) {
            this.filter = filter;
        }
        
        @SafeVarargs
        public FilterBuilder(Class<? extends ApplicationController>... classes) {
            this.controllers = classes;
        }
        
        
        @SafeVarargs
        public final FilterBuilder<T> to( Class<? extends ApplicationController>... classes) {
            this.controllers = classes;
            if (actionNames != null) ensureControllersContainsActions();
            return this;
        }
        
        @SuppressWarnings("unchecked")
        public final FilterBuilder<T> toPackage( String packageName ) {
            if (!packageName.startsWith("app.controllers"))
                packageName = "app.controllers." + packageName;
            Reflections reflect = new Reflections(packageName);
            Set<Class<? extends ApplicationController>> classes = reflect.getSubTypesOf(ApplicationController.class);
            this.controllers = classes.toArray(new Class[0]);
            return this;
        }
        
        public FilterBuilder<T> forActions(String... actionNames) {
            this.actionNames = actionNames;
            if (this.controllers != null) {
                ensureControllersContainsActions();
            }
            return this;
        }
        
        public FilterBuilder<T> with(T filter) {
            this.filter = filter;
            return this;
        }
        
        private void ensureControllersContainsActions() {
            for (Class<? extends ApplicationController> controller : controllers) {
                if (!doesControllerHaveActions(controller, actionNames))
                    throw new IllegalArgumentException("Controller does not have an action resembling the provided - " + controller.getSimpleName());
            }
        }
        private boolean doesControllerHaveActions(Class<? extends ApplicationController> controller, String[] actions) {
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
        
        Filter get(Class<? extends ApplicationController> controller, String action) {
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
        Filter get(Class<? extends ApplicationController> controller) {
            if (actionNames != null) return null;
            if (controllers == null) return filter; // the filter it defined globally
            
            return getFilter(controller);
        }
        
        Filter getFilter(Class<? extends ApplicationController> controller) {
            for (Class<? extends ApplicationController> con : controllers) {
                if (con.getName().equals(controller.getName())) {
                    return filter;
                }
            }
            return null;
        }
    }
    
    public static class SecureBuilder extends FilterBuilder<SecurityFilter> {
        public SecureBuilder() {
        }
        public SecureBuilder(SecurityFilter filter) {
            super(filter);
        }
        
        public SecureBuilder onRole(String role) {
            filter.onRole(role);
            return this;
        }
        
        public SecureBuilder redirectWhenNotLoggedIn(String toUrl) {
            filter.redirectWhenNotLoggedIn(toUrl);
            return this;
        }
        
        public SecureBuilder redirectWhenNotAuth(String toUrl) {
            filter.redirectWhenNotAuth(toUrl);
            return this;
        }
        
        public SecureBuilder logoutUrl(String url) {
            filter.logoutUrl(url);
            return this;
        }
        
    }
    
}
