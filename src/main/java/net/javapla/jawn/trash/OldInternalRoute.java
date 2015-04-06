package net.javapla.jawn.trash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.javapla.jawn.util.StringUtil;

/**
 * Could just be a singleton
 * @author MTD
 */
public class OldInternalRoute {/*extends NewRouteMatcher {
    
    private List<Pattern> standardRoutePatterns;
    private final List<List<String>> parameters;
    
    public InternalRoute(String... uris) {
        super("");
        standardRoutePatterns = new ArrayList<>();
        parameters = new ArrayList<>();
        
        for (String uri : uris) {
            parameters.add(parseParameters(uri));
            standardRoutePatterns.add(Pattern.compile(convertRawUriToRegex(uri)));
        }
    }
    
    
    public List<ControllerMap<String,String>> findMatches(String request) {
        List<ControllerMap<String, String>> matches = new ArrayList<>();
        
        for (int i = 0; i < standardRoutePatterns.size(); i++) {
            Pattern pattern = standardRoutePatterns.get(i);
            Matcher matcher = pattern.matcher(request);
            if (matcher.matches()) {
                Map<String, String> params = getPathParametersEncoded(matcher, parameters.get(i));
                matches.add( new Controller(params));
            }
        }
        
        return matches;
    }
    
    public class Controller {
        public final Map<String,String> params;
        
        public Controller(Map<String,String> params) {
            this.params = params;
        }
        
        public String getPackage() {
            String p = params.get("package");
            if (p != null) return p.replace('/', '.');
            return null;
        }
        public String getController() {
            return params.getOrDefault("controller", NewRoute.ROOT_CONTROLLER_NAME);
        }
        public String getAction() {
            return params.getOrDefault("action", NewRoute.DEFAULT_ACTION_NAME);
        }
        
        public String getControllerClassName() {
            String controllerName = getController();
            String packagePrefix = getPackage();
            
            String name = controllerName.replace('-', '_');
            String temp = "app.controllers"; //README might be a part of some configuration
            if (packagePrefix != null) {
                temp += "." + packagePrefix;
            }
            return temp + "." + StringUtil.camelize(name) + "Controller";
        }
        
        @Override
        public String toString() {
            return params.toString();
        }
    }
    
    public static void main(String[] args) {
        InternalRoute tc = new InternalRoute(
                "/{controller}/{action}/{id}",
                "/{controller}/{action}",
                "/{controller}",
                "/{package: .*?}/{controller}/{action}/{id}",
                "/{package: .*?}/{controller}/{action}",
                "/{package: .*?}/{controller}"
                );
        
        System.out.println(tc.findMatches("/cccontroller/aaaction/iiid999"));
        System.out.println(tc.findMatches("/cccontroller/aaaction"));
        System.out.println(tc.findMatches("/cccc"));
        System.out.println(tc.findMatches("/sub/sub/package/ccccontroller"));
        System.out.println(tc.findMatches("/sub/sub/package/ccccontroller/aaaction"));
        System.out.println(tc.findMatches("/sub/sub/package/ccccontroller/aaaction/iiiid999"));
    }*/

}

