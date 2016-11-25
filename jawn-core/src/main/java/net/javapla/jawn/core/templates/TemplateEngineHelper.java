package net.javapla.jawn.core.templates;

import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.routes.RouterHelper;
import net.javapla.jawn.core.util.Constants;

public class TemplateEngineHelper {

    /**
     * If the template is specified by the user with a suffix corresponding to the template engine suffix, then this is 
     * removed before returning the template.
     * Suffixes such as .st or .ftl.html
     * @param route
     * @param response
     * @return
     *//*
    public static String getTemplateForResult(Route route, Response response, final String templateRootFolder, final String templateSuffix) {
        String template = response.template();
        if (template == null) {
            if (route != null) {
                String controllerPath = RouterHelper.getReverseRoute(route.getController());
                
                // Look for a controller named template first
                if (new File(templateRootFolder + TemplateEngine.TEMPLATES_FOLDER + controllerPath+templateSuffix).exists()) {
                    return controllerPath;
                }
                
                //TODO Route(r).reverseRoute
                template =  controllerPath + "/" + route.getActionName();
                return template;
            } else {
                return null;
            }
        } else {
            if (template.endsWith(templateSuffix)) {
                return template.substring(0, template.length()-templateSuffix.length());
            }
            return template;
        }
    }*/
    
    /**
     * Getting the controller from the route
     * @param route
     * @return
     */
    public static String getControllerForResult(Route route) {
        if (route == null) return Constants.ROOT_CONTROLLER_NAME;
        return RouterHelper.getReverseRouteFast(route.getController()).substring(1);
    }
    
}
