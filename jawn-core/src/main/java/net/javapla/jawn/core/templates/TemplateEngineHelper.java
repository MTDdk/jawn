package net.javapla.jawn.core.templates;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.routes.RouterHelper;
import net.javapla.jawn.core.util.Constants;

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
        if (route == null) return Constants.ROOT_CONTROLLER_NAME;
        return RouterHelper.getReverseRoute(route.getController()).substring(1);
    }
    
}
