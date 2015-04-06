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
package net.javapla.jawn.trash;

import static net.javapla.jawn.ControllerFactory.createControllerInstance;
import static net.javapla.jawn.ControllerFactory.getControllerClassName;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.javapla.jawn.AppController;
import net.javapla.jawn.HttpMethod;
import net.javapla.jawn.RequestContext;
import net.javapla.jawn.exceptions.ClassLoadException;
import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.RouteException;
import net.javapla.jawn.i18n.Lang;
import net.javapla.jawn.util.CollectionUtil;
import net.javapla.jawn.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for looking at a URI and creating a route to controller if one is found.
 * This is a thread - safe class.
 *
 * @author Igor Polevoy
 * @author MTD
 */
@Deprecated
public class Router {

//    private static Logger LOGGER = LoggerFactory.getLogger(Router.class.getName());
//
//    public static final String CONTROLLER_NAME = "controller_name";
//    public static final String PACKAGE_SUFFIX = "package_suffix";
//
//    private final String rootControllerName;
////    private final String defaultLanguage;
////    private final String[] supportedLanguages;
//    private List<RouteBuilder> builders = new ArrayList<RouteBuilder>();
//    private List<IgnoreSpec> ignoreSpecs;
//    
//    private final ControllerRegistry controllerRegistry;
//    private final Lang languages;
//
//    protected Router(String rootControllerName, ControllerRegistry controllerRegistry, Lang languages/*String[] languages*/) {
//        this.rootControllerName = rootControllerName;
////        this.defaultLanguage = languages != null ? languages[0] : null;
////        this.supportedLanguages = languages;
//        
//        this.controllerRegistry = controllerRegistry;
//        this.languages = languages;
//    }
//
//    /**
//     * Sets custom routes
//     *
//     * @param builders set of custom routes defined for app.
//     */
//    public void setRoutes(List<RouteBuilder> builders) {
//        this.builders = builders;
//    }
//    
//    
//    /**
//     * This is a main method for recognizing a route to a controller; used when a request is received.
//     *
//     * @param uri        URI of incoming request.
//     * @param httpMethod http method of the request.
//     * @return instance of a <code>Route</code> if one is found, null if not.
//     * @throws ClassLoadException If the translation from URL to class could not be done
//     */
//    protected Route recognize(String uri, HttpMethod httpMethod, RequestContext requestContext) throws ClassLoadException {
//        Route route = matchCustom(uri, httpMethod, requestContext);
//        if (route == null) { //proceed to built-in routes
//            
//            //MTD: we first look for a language prefix and strip the URI if it is found
//            String language = findLanguagePrefix(uri);
//            if (!StringUtil.blank(language)) {
//                uri = uri.substring(language.length() +1 );
//                if (uri.isEmpty()) uri = "/";
//            } else {
//                language = languages.getDefaultLanguage();//defaultLanguage;
//            }
//            
//            //DTO as map here
//            Map<String, String> controllerPath = getControllerPath(uri);
//
//            String controllerName = controllerPath.get(Router.CONTROLLER_NAME);
//            String packageSuffix = controllerPath.get(Router.PACKAGE_SUFFIX);
//            if (controllerName == null) {
//                return null;
//            }
//            String controllerClassName = getControllerClassName(controllerName, packageSuffix);
//            AppController controller = createControllerInstance(controllerClassName);
//
//            if (uri.equals("/") && rootControllerName != null && httpMethod.equals(HttpMethod.GET)) {
//                route = new Route(language, controller);
//            }else{
//                route = /*controller.restful() ? matchRestful(uri, controllerName, packageSuffix, httpMethod, controller) :*/
//                        matchStandard(uri, controllerName, packageSuffix, httpMethod, controller, language);
//            }
//            
//            if (route == null)
//                throw new RouteException("Failed to map resource to URI: " + uri);
//        }
//
//        route.setIgnoreSpecs(ignoreSpecs);
//        return route;
//    }
//
//    private Route matchCustom(String uri, HttpMethod httpMethod, RequestContext requestContext) throws ClassLoadException {
//        for (RouteBuilder builder : builders) {
//            if (builder.matches(uri, httpMethod, requestContext)) {
//                return new Route(builder);
//            }
//        }
//        return null;
//    }
//
//
//    /**
//     * Will match a standard, non-restful route.
//     *
//     * @param uri            request URI
//     * @param controllerName name of controller
//     * @param packageSuffix  package suffix or null if none. .
//     * @return instance of a <code>Route</code> if one is found, null if not.
//     */
//    private Route matchStandard(String uri, String controllerName, String packageSuffix, HttpMethod httpMethod, AppController controller, String language) {
//
//        String controllerPath = (packageSuffix != null ? "/" + packageSuffix.replace(".", "/") : "") + "/" + controllerName;
//        String theUri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
//
//        //ANY    /package_suffix/controller
//        if (controllerPath.length() == theUri.length()) {
//            return new Route(language, controller);
//        }
//
//        String tail = theUri.substring(controllerPath.length() + 1);
//        String[] parts = split(tail, "/");
//
//        //ANY    /package_suffix/controller/action
//        if (parts.length == 1) {
//            return new Route(language, controller, /*httpMethod + */parts[0], httpMethod);
//        }
//
//        //ANY    /package_suffix/controller/action/id/
//        if (parts.length == 2) {
//            return new Route(language, controller, /*httpMethod + */parts[0], parts[1], httpMethod);
//        }
//        LOGGER.warn("Failed to find action for request: " + uri);
//        return null;
//    }
//
//
//    /**
//     * Will match a restful route.
//     *
//     * @param uri            request URI
//     * @param controllerName name of controller
//     * @param packageSuffix  package suffix or null if none. .
//     * @param httpMethod     http method of a request.
//     * @return instance of a <code>Route</code> if one is found, null if not.
//     */
//    /*private Route matchRestful(String uri, String controllerName, String packageSuffix, HttpMethod httpMethod, AppController controller) {
//
//        String theUri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
//        String controllerPath = (packageSuffix != null ? "/" + packageSuffix.replace(".", "/") : "") + "/" + controllerName;
//        String tail = theUri.length() > controllerPath.length() ? theUri.substring(controllerPath.length() + 1) : "";
//        String[] parts = split(tail, "/");
//
//        //GET 	/photos 	            index 	display a list of all photos
//        if (controllerPath.equals(theUri) && httpMethod.equals(HttpMethod.GET)) {
//            return new Route(controller);
//        }
//
//        //GET 	/photos/new_form 	    new_form        return an HTML form for creating a new photo
//        if (parts.length == 1 && httpMethod.equals(HttpMethod.GET) && parts[0].equalsIgnoreCase("new_form")) {
//            return new Route(controller, "new_form");
//        }
//
//        //POST 	/photos 	            create 	        create a new photo
//        if (parts.length == 0 && httpMethod.equals(HttpMethod.POST)) {
//            return new Route(controller, "create");
//        }
//
//        //GET 	/photos/id 	        show            display a specific photo
//        if (parts.length == 1 && httpMethod.equals(HttpMethod.GET)) {
//            return new Route(controller, "show", parts[0]);
//        }
//
//        //GET 	/photos/id/edit_form   edit_form 	    return an HTML form for editing a photo
//        if (parts.length == 2 && httpMethod.equals(HttpMethod.GET) && parts[1].equalsIgnoreCase("edit_form")) {
//            return new Route(controller, "edit_form", parts[0]);
//        }
//
//        //PUT 	/photos/id 	        update          update a specific photo
//        if (parts.length == 1 && httpMethod.equals(HttpMethod.PUT)) {
//            return new Route(controller, "update", parts[0]);
//        }
//
//        //DELETE 	/photos/id 	        destroy         delete a specific photo
//        if (parts.length == 1 && httpMethod.equals(HttpMethod.DELETE)) {
//            return new Route(controller, "destroy", parts[0]);
//        }
//        LOGGER.warn("Failed to find action for request: " + uri);
//        return null;
//    }*/
//
//
//    /**
//     * Generates a URI for a controller.
//     *
//     * @param controllerPath path to controller.
//     * @param action         action for a controller
//     * @param id             id on a URI
//     * @param restful        true if a route for a restful controller is needed, false for non-restful.
//     * @param params         name/value pairs to be used to form a query string.
//     * @return formed URI based on arguments.
//     */
//    public static String generate(String controllerPath, String action, String id, /*boolean restful,*/ Map<String, String> params) {
//
//        //prepend slash if missing
//        StringBuilder uri = new StringBuilder(controllerPath.startsWith("/") ? controllerPath : "/" + controllerPath);
//
//        /*if (restful) {
//            if (action != null && !(action.equals("new_form") || action.equals("edit_form"))) {
//                throw new IllegalArgumentException("Illegal action name: '" + action +
//                        "', allowed names for restful controllers: 'new_form' and 'edit_form'");
//            }
//
//            if (action != null && action.equals("new_form") && id != null) {
//                throw new IllegalArgumentException("Cannot provide ID to action 'new_form'");
//            }
//
//            if (action != null && action.equals("edit_form") && id == null) {
//                throw new IllegalArgumentException("Must provide ID to action 'edit_form'");
//            }
//
//            if (id != null) {
//                uri.append("/").append(id);
//            }
//
//            if (action != null) {
//                uri.append("/").append(action);
//            }
//        } else*/ {
//            if (action != null) {
//                uri.append("/").append(action);
//            }
//
//            if (id != null) {
//                uri.append("/").append(id);
//            }
//        }
//
//        if (params.size() > 0) {
//            uri.append("?");
//        }
//
//        List<String> pairs = new ArrayList<String>();
//
//        for (Object key : params.keySet()) {
//            try {
//                // Modified by MTD
//                pairs.add(URLEncoder.encode(key.toString(), StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(params.get(key).toString(), StandardCharsets.UTF_8.name()));
//            } catch (Exception ignore) {/* By using StandardCharsets an exception ought not occur*/}
//        }
//        //sorting to make hard-coded tests pass
//        Collections.sort(pairs, new Comparator<String>() {
//            public int compare(String o1, String o2) {
//                return o1.compareTo(o2);
//            }
//        });
//
//        uri.append(StringUtil.join(pairs, "&"));
//
//        return uri.toString();
//    }
//
//
//    /**
//     * Finds a controller path from URI. Controller path includes a package prefix taken from URI, similar to:
//     * <p>
//     * <code>http://host/context/admin/printers/show/1</code>, where "admin" is a "package_suffix", "printers" is a
//     * "controller_name".
//     * <p>
//     * for example above, the method will Map with two keys: "package_suffix" and "controller_name"
//     *
//     * @param uri this is a URI - the information after context : "controller/action/whatever".
//     * @return map with two keys: "controller_name" and "package_suffix", both of which can be null.
//     */
//    protected Map<String, String> getControllerPath(String uri) {
//        
//        boolean rootPath = uri.equals("/") || uri.isEmpty();
//        boolean useRootController = rootPath && rootControllerName != null;
//
//        if (useRootController) {
//            return CollectionUtil.map(CONTROLLER_NAME, rootControllerName);
//        } else if (rootControllerName == null && rootPath) {
//            LOGGER.warn("URI is: '/', but root controller not set");
//            return new HashMap<String, String>();
//        } else {
//            // in order for the package_suffix to be effective
//            // we need the uri to be at least: /package/whatever[/..]
//            // so if the split is only of length 2 (we covered length 1 in rootPath)
//            // then it is must be a controller outside of a package
//            if (uri.split("/").length > 2) {
//                String pack;
//                if ((pack = findPackagePrefix(uri)) != null) {
//    //                try {
//                        String controllerName = findControllerNamePart(pack, uri);
//                        return CollectionUtil.map(CONTROLLER_NAME, controllerName, PACKAGE_SUFFIX, pack);
//    //                } catch (ControllerException e) {
//                        // a package was found with the name, but no controller in it
//                        // we try to return it as a controller instead
//    //                }
//                }
//            } //else {
//                return CollectionUtil.map(CONTROLLER_NAME, uri.split("/")[1]);//no package suffix
//            //}
//        }
//    }
//
//    /**
//     * Generates a path to a controller based on its package and class name. The path always starts with a slash: "/".
//     * Examples:
//     * <p></p>
//     * <ul>
//     * <li>For class: <code>app.controllers.Simple</code> the path will be: <code>/simple</code>.</li>
//     * <li>For class: <code>app.controllers.admin.PeopleAdmin</code> the path will be: <code>/admin/people_admin</code>.</li>
//     * <li>For class: <code>app.controllers.admin.simple.PeopleAdmin</code> the path will be: <code>/admin/simple/people_admin</code>.</li>
//     * </ul>
//     * <p></p>
//     * Class name looses the "Controller" suffix and gets converted to underscore format, while packages stay unchanged.
//     *
//     * @param controllerClass class of a controller.
//     * @param <T> class extending {@link AppController}
//     * @return standard path for a controller.
//     */
//    public static <T extends AppController> String getControllerPath(Class<T> controllerClass) {
//        String simpleName = controllerClass.getSimpleName();
//        if (!simpleName.endsWith("Controller")) {
//            throw new ControllerException("controller name must end with 'Controller' suffix");
//        }
//
//        String className = controllerClass.getName();
//        if (!className.startsWith("app.controllers")) {
//            throw new ControllerException("controller must be in the 'app.controllers' package");
//        }
//        String packageSuffix = className.substring("app.controllers".length(), className.lastIndexOf("."));
//        packageSuffix = packageSuffix.replace(".", "/");
//        if (packageSuffix.startsWith("/"))
//            packageSuffix = packageSuffix.substring(1);
//
//        return (packageSuffix.equals("") ? "" : "/" + packageSuffix) + "/" + StringUtil.underscore(simpleName.substring(0, simpleName.lastIndexOf("Controller")));
//    }
//
//    /**
//     * Now that we know that this controller is under a package, need to find the controller short name.
//     *
//     * @param pack part of the package of the controller, taken from URI: value between "app.controllers" and controller name.
//     * @param uri  uri from request
//     * @return controller name
//     */
//    protected static String findControllerNamePart(String pack, String uri) throws ControllerException {
//        String temp = uri.startsWith("/") ? uri.substring(1) : uri;
//        temp = temp.replace("/", ".");
//        if (temp.length() > pack.length())
//            temp = temp.substring(pack.length() + 1);
//
//        if (temp.equals("") || temp.equals(pack))
//            throw new ControllerException("You defined a controller package '" + pack + "', but this request does not specify controller name");
//
//        return temp.split("\\.")[0];
//    }
//
//    /**
//     * Finds a part of a package name which can be found in between "app.controllers" and short name of class.
//     *
//     * @param uri uri from request
//     * @return a part of a package name which can be found in between "app.controllers" and short name of class, or null
//     *         if not found
//     */
//    protected String findPackagePrefix(String uri) {
//
//        String temp = uri.startsWith("/") ? uri.substring(1) : uri;
//        temp = temp.replace(".", "_");
//        temp = temp.replace("/", ".");
//
//        //find all matches
//        List<String> candidates = new ArrayList<String>();
//        
//        for (String pack : /*Context.getControllerRegistry()*/controllerRegistry.getControllerPackages()) {
//            if (temp.startsWith(pack)) {
//                candidates.add(pack);
//            }
//        }
//        int resultIndex = 0;
//        int size = 0;
//        //find the longest package
//        for (int i = 0; i < candidates.size(); i++) {
//            String candidate = candidates.get(i);
//            if (candidate.length() > size) {
//                size = candidate.length();
//                resultIndex = i;
//            }
//        }
//        return candidates.size() > 0 ? candidates.get(resultIndex) : null;
//    }
//    
//    /**
//     * Finds the language segment of the URI if the language property is set.
//     * Just extracts the first segment.
//     * @param uri The full URI
//     * @return the extracted language segment. null, if none is found
//     * @author MTD
//     */
//    protected String findLanguagePrefix(String uri) {
//        if (languages.getDefaultLanguage()/*defaultLanguage */== null) return null;
//        String lang = uri.startsWith("/") ? uri.substring(1) : uri;
//        lang = lang.split("/")[0];
//        
//        if(languages.isLanguageSupported(lang)) return lang;
////        for (String l : supportedLanguages) {
////            if (lang.equals(l))
////                return lang;
////        }
//        
//        //TODO we probably want to throw some exceptions
//        return null;
//    }
//
//    //TODO: write a regexp one day
//    private static String[] split(String value, String delimeter) {
//        StringTokenizer st = new StringTokenizer(value, delimeter);
//        String[] res = new String[st.countTokens()];
//        for (int i = 0; st.hasMoreTokens(); i++) {
//            res[i] = st.nextToken();
//        }
//        return res;
//    }
//
//    public void setIgnoreSpecs(List<IgnoreSpec> ignoreSpecs) {
//        this.ignoreSpecs = ignoreSpecs;
//    }
}
