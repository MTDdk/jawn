package net.javapla.jawn.templates;

import net.javapla.jawn.NewControllerResponse;
import net.javapla.jawn.NewRoute;
import net.javapla.jawn.RouterHelper;

public class TemplateEngineHelper {

    public static String getTemplateForResult(NewRoute route, NewControllerResponse response) {
        if (response.template() == null) {
        
            String controllerPath = RouterHelper.getReverseRoute(route.getController().getClass());
            
          //TODO Route(r).reverseRoute
            String template =  controllerPath + "/" + route.getActionName();
            return template;
        } else {
            return response.template();
        }
    }
    
}
