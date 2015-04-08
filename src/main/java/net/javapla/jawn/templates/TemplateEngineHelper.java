package net.javapla.jawn.templates;

import net.javapla.jawn.ControllerResponse;
import net.javapla.jawn.Route;
import net.javapla.jawn.RouterHelper;

public class TemplateEngineHelper {

    public static String getTemplateForResult(Route route, ControllerResponse response) {
        if (response.template() == null) {
            if (route != null) {
                String controllerPath = RouterHelper.getReverseRoute(route.getController().getClass());
                
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
