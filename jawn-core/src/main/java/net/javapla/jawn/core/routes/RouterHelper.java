package net.javapla.jawn.core.routes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.core.Controller;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.util.PropertiesConstants;
import net.javapla.jawn.core.util.StringUtil;

public class RouterHelper {

    
    /**
     * Generates a path to a controller based on its package and class name. The path always starts with a slash: "/".
     * Examples:
     * <p>
     * <ul>
     * <li>For class: <code>app.controllers.Simple</code> the path will be: <code>/simple</code>.</li>
     * <li>For class: <code>app.controllers.admin.PeopleAdmin</code> the path will be: <code>/admin/people_admin</code>.</li>
     * <li>For class: <code>app.controllers.admin.simple.PeopleAdmin</code> the path will be: <code>/admin/simple/people_admin</code>.</li>
     * </ul>
     * <p>
     * Class name looses the "Controller" suffix and gets converted to underscore format, while packages stay unchanged.
     *
     * @param controllerClass class of a controller.
     * @param <T> class extending {@link Controller}
     * @return standard path for a controller.
     */
    public static String getReverseRoute(Class<? extends Controller> clazz) {
        String simpleName = clazz.getSimpleName();
        if (! simpleName.endsWith("Controller")) {
            throw new ControllerException("controller name must end with 'Controller' suffix");
        }

        String className = clazz.getName();
        if (!className.startsWith(PropertiesConstants.CONTROLLER_PACKAGE)) {
            throw new ControllerException("controller must be in the 'app.controllers' package");
        }
        String packageSuffix = className.substring(PropertiesConstants.CONTROLLER_PACKAGE.length(), className.lastIndexOf("."));
        packageSuffix = packageSuffix.replace(".", "/");
        if (packageSuffix.startsWith("/"))
            packageSuffix = packageSuffix.substring(1);

        return (packageSuffix.equals("") ? "" : "/" + packageSuffix) + "/" + StringUtil.underscore(simpleName.substring(0, simpleName.lastIndexOf("Controller")));
    }
    
    private static final int CONTROLLER_PACKAGE_LENGTH = PropertiesConstants.CONTROLLER_PACKAGE.length();
    private static final int CONTROLLER_LENGTH = "Controller".length();
    public static String getReverseRouteFast(Class<? extends Controller> clazz) {
    	String simpleName = clazz.getSimpleName();
        if (! simpleName.endsWith("Controller")) {
            throw new ControllerException("controller name must end with 'Controller' suffix");
        }

        String className = clazz.getName();
        if (!className.startsWith(PropertiesConstants.CONTROLLER_PACKAGE)) {
            throw new ControllerException("controller must be in the 'app.controllers' package");
        }
    	String packageSuffix = className.substring(CONTROLLER_PACKAGE_LENGTH, className.lastIndexOf("."));
        packageSuffix = packageSuffix.replace(".", "/");
        if (packageSuffix.startsWith("/"))
            packageSuffix = packageSuffix.substring(1);

        return (packageSuffix.equals("") ? "" : "/" + packageSuffix) + "/" + StringUtil.underscore(simpleName.substring(0, simpleName.length()-CONTROLLER_LENGTH));
    }
    
    /**
     * Generates a URI for a controller.
     *
     * @param controllerPath path to controller.
     * @param action         action for a controller
     * @param id             id on a URI
     * @param params         name/value pairs to be used to form a query string.
     * @return formed URI based on arguments.
     */
    public static String generate(String controllerPath, String action, String id,  Map<String, String> params) {

        //prepend slash if missing
        StringBuilder uri = new StringBuilder(controllerPath.startsWith("/") ? controllerPath : "/" + controllerPath);

        if (action != null) {
            uri.append("/").append(action);
        }

        if (id != null) {
            uri.append("/").append(id);
        }

        if (params.size() > 0) {
            uri.append("?");
        }

        List<String> pairs = new ArrayList<String>();

        for (Object key : params.keySet()) {
            try {
                // Modified by MTD
                pairs.add(URLEncoder.encode(key.toString(), StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(params.get(key).toString(), StandardCharsets.UTF_8.name()));
            } catch (Exception ignore) {/* By using StandardCharsets an exception ought not occur*/}
        }

        uri.append(StringUtil.join(pairs, "&"));

        return uri.toString();
    }
}
