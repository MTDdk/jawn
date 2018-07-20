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
    
    public T locateLayoutTemplate(String controller, final String layout) throws ViewException {
        return locateLayoutTemplate(controller, layout, false);
    }

    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    public T locateLayoutTemplate(String controller, final String layout, boolean useCache) throws ViewException {
        // first see if we have already looked for the template
        final String controllerLayoutCombined = controller + '/' + layout;
        if (useCache && cachedTemplates.containsKey(controllerLayoutCombined)) 
            return engine.clone(cachedTemplates.get(controllerLayoutCombined)); // README: computeIfAbsent does not save the result if null - which it actually might need to do to minimise disk reads
        
        
        T template;
        if ((template = lookupLayoutWithNonDefaultName(controllerLayoutCombined)) != null) 
            return cacheTemplate(controllerLayoutCombined, template, useCache);
        
        // going for defaults
        if ((template = lookupDefaultLayoutInControllerPathChain(controller)) != null) 
            return cacheTemplate(controllerLayoutCombined, template, useCache);
        
        if ((template = lookupDefaultLayout()) != null) 
            return cacheTemplate(controllerLayoutCombined, template, useCache);
        
        throw new ViewException(TemplateEngine.LAYOUT_DEFAULT + engine.getSuffixOfTemplatingEngine() + " is not to be found anywhere");
    }
    
    private T lookupLayoutWithNonDefaultName(final String controllerLayoutCombined) {
        // look for a layout of a given name within the controller folder
        return engine.readTemplate(controllerLayoutCombined);
    }
    
    private T lookupDefaultLayoutInControllerPathChain(String controller) {
        // look for the default layout in controller folder
        // and bubble up one folder if nothing is found
        // (controller can be: /folder1/folder2/folder3 - so it is possible to bubble up)
        T template;
        while ((template = engine.readTemplate(controller + '/' + TemplateEngine.LAYOUT_DEFAULT)) == null && controller.indexOf('/') > 0) {
            controller = controller.substring(0, controller.lastIndexOf('/'));
        }
        return template;
    }
    
    private T lookupDefaultLayout() {
        // last resort - this should always be present
        return engine.readTemplate(TemplateEngine.LAYOUT_DEFAULT);
    }
    
    private T cacheTemplate(final String controllerLayoutCombined, final T template, final boolean useCache) {
        if (useCache) {
            cachedTemplates.putIfAbsent(controllerLayoutCombined, template);
        }
        return template;
    }
}
