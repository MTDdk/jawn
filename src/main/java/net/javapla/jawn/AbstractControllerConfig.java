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
import java.util.List;

import net.javapla.jawn.trash.controller_filters.ControllerFilter;

/**
 * This class is to be sub-classed by the application level class called <code>app.config.AppControllerConfig</code>.
 * This class provides ways to bind filters to controllers. It has coarse grain methods for binding as well as
 * fine grained.
 *
 * <p>
 * See {@link net.javapla.jawn.trash.controller_filters.ControllerFilter}.
 * </p>
 *
 * <p>
 * <strong>Filters before() methods are executed in the same order as filters are registered.</strong>
 * </p>
 *
 * <ul>
 *      <li> Adding global filters:{@link #addGlobalFilters(net.javapla.jawn.trash.controller_filters.ControllerFilter...)}
 *      <li> Adding controller filters:{@link #add(net.javapla.jawn.trash.controller_filters.ControllerFilter...)} )}
 * </ul>
 * Adding a global filter adds it to all controllers. It makes sense to use this to add timing filters, logging filters,
 * etc.
 *
 * <p>
 * <strong>Filters' after() methods are executed in the opposite order as filters are registered.</strong>
 * </p>
 *
 *
 * <p>
 * Here is an example of adding a filter to specific actions:
 * </p>
 * <pre>
 * add(mew TimingFilter(), new DBConnectionFilter()).to(PostsController.class).forActions("index", "show");
 * </pre>
 *
 *
 * @author Igor Polevoy
 * @author MTD
 */
@Deprecated
public abstract class AbstractControllerConfig extends AppConfig {

    //exclude some controllers from filters
    private final List<ExcludeBuilder> excludeBuilders;
    
    private final List<FilterBuilder> filterBuilders;
    
    public AbstractControllerConfig() {
        this.excludeBuilders = new ArrayList<>();
        this.filterBuilders = new ArrayList<>();
    }

    public class FilterBuilder {
        private ControllerFilter[] filters;
        private Class<? extends AppController>[] controllerClasses;
        private String[] actionNames;
        private String[] excludedActions;

        protected FilterBuilder(ControllerFilter[] filters) {
            this.filters = filters;
        }

        /**
         * Provides a list of controllers to which filters are added.
         *
         * @param controllerClasses list of controller classes to which filters are added.
         * @return self, usually to run a method {@link #forActions(String...)}.
         */
        @SafeVarargs
        public final FilterBuilder to(Class<? extends AppController>... controllerClasses) {
            this.controllerClasses = controllerClasses;
//            for (Class<? extends AppController> controllerClass : controllerClasses) {
//                Context.getControllerRegistry().getMetaData(controllerClass).addFilters(filters);
//            }
            return this;
        }

        /**
         * Adds a list of actions for which filters are configured.
         * <br>
         * Example:
         * <pre>
         * add(mew TimingFilter(), new DBConnectionFilter()).to(PostsController.class).forActions("index", "getShow");
         * </pre>
         *
         *
         * @param actionNames list of action names for which filters are configured.
         */
        public void forActions(String... actionNames) {
            if (controllerClasses == null)//README this might not be necessary anymore due to the new nature of the class
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'forActions(actions)'");

            this.actionNames = actionNames;
//            for (Class<? extends AppController> controllerClass : controllerClasses) {
//                Context.getControllerRegistry().getMetaData(controllerClass).addFilters(filters, actionNames);
//            }
        }

        /**
         * Excludes actions from filter configuration. As opposed to {@link #forActions(String...)}.
         * <p>
         * Example:
         * <pre>
         * add(mew TimingFilter()).to(PostsController.class).excludeActions("postShow", "getList");
         * </pre>
         *
         * @param excludedActions list of actions for which this filter will not apply.
         */
        public void excludeActions(String... excludedActions) {
            if (controllerClasses == null)
                throw new IllegalArgumentException("controller classes not provided. Please call 'to(controllers)' before 'exceptAction(actions)'");

            this.excludedActions = excludedActions;
//            for (Class<? extends AppController> controllerClass : controllerClasses) {
//                Context.getControllerRegistry().getMetaData(controllerClass).addFiltersWithExcludedActions(filters, excludedActions);
//            }

        }
        
        // only called by parent class
        private void call(ControllerRegistry controllerRegistry) {
            for (Class<? extends AppController> controllerClass : controllerClasses) {
                ControllerMetaData metaData = controllerRegistry.getMetaData(controllerClass);
                
                metaData.addFilters(filters);
                
                // these ought to be mutual exclusive
                if (actionNames != null)
                    metaData.addFilters(filters, actionNames);
                else if (excludedActions != null)
                    metaData.addFiltersWithExcludedActions(filters, excludedActions);
            }
        }
    }


    /**
     * Adds a set of filters to a set of controllers.
     * The filters are invoked in the order specified.
     *
     * @param filters filters to be added.
     * @return object with <code>to()</code> method which accepts a controller class. The return type is not important and not used by itself.
     */
    protected FilterBuilder add(ControllerFilter... filters) {
        FilterBuilder builder = new FilterBuilder(filters);
        filterBuilders.add(builder);
        return builder;
    }

    /**
     * Adds filters to all controllers globally.
     * Example of usage:
     * <pre>
     * ...
     *   addGlobalFilters(new TimingFilter(), new DBConnectionFilter());
     * ...
     * </pre>
     *
     * @param filters filters to be added.
     * @return {@link ExcludeBuilder}
     */
    protected ExcludeBuilder addGlobalFilters(ControllerFilter... filters) {
        ExcludeBuilder excludeBuilder = new ExcludeBuilder(filters);
        excludeBuilders.add(excludeBuilder);
        return excludeBuilder;
    }

    @Override
    public void completeInit(ControllerRegistry controllerRegistry) {

        for (ExcludeBuilder excludeBuilder : excludeBuilders) {
            /*Context.getControllerRegistry()*/controllerRegistry.addGlobalFilters(excludeBuilder.getFilters(), excludeBuilder.getExcludeControllerClasses());
        }
        
        for (FilterBuilder filterBuilder : filterBuilders) {
            filterBuilder.call(controllerRegistry);
        }
    }


    public class ExcludeBuilder{

        private List<Class<? extends AppController>> excludeControllerClasses = new ArrayList<Class<? extends AppController>>();
        private List<ControllerFilter> filters = new ArrayList<ControllerFilter>();

        public ExcludeBuilder(ControllerFilter[] filters) {
            this.filters.addAll(Arrays.asList(filters));
        }

        /**
         * Pass controllers to this method if you want to exclude supplied filters to be applied to them.
         *
         * @param excludeControllerClasses list of controllers to which these filters do not apply.
         */
        public void exceptFor(Class<? extends AppController>[] excludeControllerClasses) {
            this.excludeControllerClasses.addAll(Arrays.asList(excludeControllerClasses));
        }

        public List<Class<? extends AppController>> getExcludeControllerClasses() {
            return excludeControllerClasses;
        }

        public List<ControllerFilter> getFilters() {
            return filters;
        }

    }

}
