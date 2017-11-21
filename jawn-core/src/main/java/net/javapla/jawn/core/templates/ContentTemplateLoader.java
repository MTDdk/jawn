package net.javapla.jawn.core.templates;

import java.io.File;
import java.util.HashMap;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.exceptions.ViewException;
import net.javapla.jawn.core.routes.Route;
import net.javapla.jawn.core.routes.RouterHelper;

public class ContentTemplateLoader<T> {
    
    private final String realPath;
    private final TemplateEngine.TemplateRenderEngine<T> engine;
    private final String templateSuffix;
    private final int templateSuffixLengthToRemove;
    private final HashMap<String,String> layoutMap = new HashMap<>();
    
    private final HashMap<String, T> cachedTemplates = new HashMap<>();
    
    public ContentTemplateLoader(DeploymentInfo info, TemplateEngine.TemplateRenderEngine<T> engine) {
        realPath = getTemplateRootFolder(info);
        this.engine = engine;
        templateSuffix = engine.getSuffixOfTemplatingEngine();
        templateSuffixLengthToRemove = templateSuffix.length() + (templateSuffix.charAt(0) == '.' ? 0 : 1); // add one if the dot is missing
    }
    
    public static final String getTemplateRootFolder(final DeploymentInfo info) {
        return info.getRealPath(TemplateEngine.TEMPLATES_FOLDER);
    }
    public final String getTemplateRootFolder() {
        return realPath;
    }

    public final String handleLayoutEndings(Result response) {
        String layout = response.layout();
        if (layout != null) {
            layout = layoutMap.computeIfAbsent(layout, (l -> {
                if (l.endsWith(templateSuffix))
                    l = l.substring(0, l.length() - templateSuffixLengthToRemove);
                if (!l.endsWith(".html"))
                    l += ".html";
                return l;
            }));
        }
        return layout;
    }
    
    /**
     * If the template is specified by the user with a suffix corresponding to the template engine suffix, then this is 
     * removed before returning the template.
     * Suffixes such as .st or .ftl.html
     * @param route
     * @param response
     * @return
     */
    public String getTemplateForResult(Route route, Result response) {
        String template = response.template();
        
        if (template == null) {
            if (route != null) {
                String controllerPath = RouterHelper.getReverseRouteFast(route.getController());
                
                // Look for a controller named template first
                if (new File(realPath + controllerPath + templateSuffix).exists()) {
                    return controllerPath;
                }
                
                //TODO Route(r).reverseRoute
                return controllerPath + "/" + route.getActionName();
            } else {
                return null;
            }
        } else {
            if (template.endsWith(templateSuffix)) {
                return template.substring(0, template.length() - templateSuffixLengthToRemove);
            }
            return template;
        }
    }
    
    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    public T locateDefaultLayout(String controller, final String layout, boolean useCache) throws ViewException {
        // look for a layout of a given name within the controller folder
        T index = readTemplate(controller + '/' + layout, useCache);
        if (index != null) {
            return index;
        }

        // going for defaults
        // look for the default layout in controller folder
        // and bubble up one folder if nothing is found
        while ((index = readTemplate(controller + '/' + TemplateEngine.LAYOUT_DEFAULT, useCache)) == null && controller.indexOf('/') > 0) {
            controller = controller.substring(0,controller.lastIndexOf('/'));
        }
        if (index != null)
            return index;

        // last resort - this should always be present
        index = readTemplate(TemplateEngine.LAYOUT_DEFAULT, useCache);
        if (index != null) return index; 
        throw new ViewException(TemplateEngine.LAYOUT_DEFAULT + engine.getSuffixOfTemplatingEngine() + " is not to be found anywhere");
    }
    
    public T locateDefaultLayout(String controller, final String layout) throws ViewException {
        return locateDefaultLayout(controller, layout, false);
    }
    
    private T readTemplate(final String path, boolean useCache) {
        if (useCache) {
            return cachedTemplates.computeIfAbsent(path, key -> engine.readTemplate(key)); // README: does not save the result if null - which it actually might need to do to minimise disk reads
        } else {
            return engine.readTemplate(path);
        }
    }
}
