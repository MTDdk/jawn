package net.javapla.jawn.core.templates;

import net.javapla.jawn.core.ControllerResponse;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.RouterHelper;

public class TemplateEngineHelper {

    public static String getTemplateForResult(Route route, ControllerResponse response) {
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
    
}
