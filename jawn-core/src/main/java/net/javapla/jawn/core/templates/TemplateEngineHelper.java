package net.javapla.jawn.core.templates;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.RouteBuilder;
import net.javapla.jawn.core.RouterHelper;

public class TemplateEngineHelper {

    public static String getTemplateForResult(Route route, Response response) {
        if (response.template() == null) {
            if (route != null) {
                String controllerPath = RouterHelper.getReverseRoute(route.getController());
                
              //TODO Route(r).reverseRoute
                String template =  controllerPath + "/" + route.getActionName();
                return template;
            } else {
                return null;
            }
        } else {
            return response.template();
        }
    }
    
    /**
     * Getting the controller from the route
     * @param route
     * @return
     */
    public static String getControllerForResult(Route route) {
        if (route == null) return RouteBuilder.ROOT_CONTROLLER_NAME;
        return RouterHelper.getReverseRoute(route.getController()).substring(1);
    }
    
}
