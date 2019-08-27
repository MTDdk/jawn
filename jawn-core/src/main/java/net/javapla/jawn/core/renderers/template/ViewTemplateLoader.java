package net.javapla.jawn.core.renderers.template;

import java.io.Writer;
import java.util.HashMap;
import java.util.function.Consumer;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.util.CharArrayList;

public class ViewTemplateLoader<T> {
    
    private final String realPath;
    private final TemplateRendererEngine<T> engine;
    private final String templateSuffix;
    private final int templateSuffixLengthToRemove;
    private final HashMap<String,String> layoutMap = new HashMap<>();
    
    private final HashMap<String, T> cachedTemplates = new HashMap<>();
    
    public ViewTemplateLoader(DeploymentInfo info, TemplateRendererEngine<T> engine) {
        realPath = getTemplateRootFolder(info);
        this.engine = engine;
        templateSuffix = engine.getSuffixOfTemplatingEngine();
        templateSuffixLengthToRemove = templateSuffix.length() + (templateSuffix.charAt(0) == '.' ? 0 : 1); // add one if the dot is missing
    }
    
    public static final String getTemplateRootFolder(final DeploymentInfo info) {
        return info.getRealPath(TemplateRendererEngine.TEMPLATES_FOLDER);
    }
    public final String getTemplateRootFolder() {
        return realPath;
    }
    
    public ViewTemplates<T> load(final View view, final boolean useCache) throws Up.ViewError {
        final String templateName = getTemplateNameForResult(view);
        final String layoutName = getLayoutNameForResult(view);
        final T template = templateName != null ? locateContentTemplate(templateName, useCache) : null;
        
        final T layout;
        if (layoutName == null) { // no layout
            // both layout and template should not be null
            if (template == null) {
                throw new Up.ViewError("Could not find the template " + templateName + ". Is it spelled correctly?");
            }
            
            layout = null;
        } else {
            layout = locateLayoutTemplate(view.path(), layoutName, useCache);
        }
        
        return new ViewTemplates<T>() {
            @Override
            public String templateName() {
                return templateName;
            }

            @Override
            public String layoutName() {
                return layoutName;
            }

            @Override
            public T template() {
                return template;
            }

            @Override
            public T layout() {
                return layout;
            }
        };
    }
    
    public void render(final Context context, Consumer<Writer> render, Consumer<Exception> errorOccurred) throws Up.ViewError {
        try (final CharArrayList writer = new CharArrayList(8192)/*stream.getWriter()*/) {
            render.accept(writer);
            
            context.resp().send(writer.toCharBuffer());
        } catch (Exception e) {
            errorOccurred.accept(e);
            throw new Up.ViewError(e);
        }
    }
    
    public void render(final Context context, Consumer<Writer> render) throws Up.ViewError {
        render(context, render, e -> {});
    }

    public final String getLayoutNameForResult(View view) {
        String layout = view.layout();
        if (layout != null) {
            // handle layout endings
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
     * @param view
     * @return
     */
    public String getTemplateNameForResult(/*Route route,*/ View view) {
        
        final String path = view.path();
        
        /*
        // Look for a controller named template first
        if (new File(realPath + path + templateSuffix).exists()) {
            return path;
        }*/
        
        String template = view.template();
        if (template.endsWith(templateSuffix)) {
            template = template.substring(0, template.length() - templateSuffixLengthToRemove);
        }
        
        return path + "/" + template;
        
        
        
        
        /*String template = response.template();
        
        if (template == null) {
            //if (route != null) {
                String controllerPath = RouterHelper.getReverseRouteFast(route.getController());
                
                // Look for a controller named template first
                if (new File(realPath + controllerPath + templateSuffix).exists()) {
                    return controllerPath;
                }
                
                //TODO Route(r).reverseRoute
                return controllerPath + "/" + route.getActionName();
            } else {
                return null;
            //}
            
        } else {
            if (template.endsWith(templateSuffix)) {
                return template.substring(0, template.length() - templateSuffixLengthToRemove);
            }
            return template;
        }*/
    }
    
    public T locateContentTemplate(final String contentTemplateName, boolean useCache) {
        if (useCache && cachedTemplates.containsKey(contentTemplateName)) 
            return engine.clone(cachedTemplates.get(contentTemplateName));
        
        T template = engine.readTemplate(contentTemplateName);
        if (template != null) return cacheTemplate(contentTemplateName, template, useCache);
            
        return null; //throws new ViewException();??
    }
    
    public T locateLayoutTemplate(final String controller, final String layout) throws Up.ViewError {
        return locateLayoutTemplate(controller, layout, false);
    }

    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    public T locateLayoutTemplate(String controller, final String layout, final boolean useCache) throws Up.ViewError {
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
        
        throw new Up.ViewError(TemplateRendererEngine.LAYOUT_DEFAULT + engine.getSuffixOfTemplatingEngine() + " is not to be found anywhere");
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
        while ((template = engine.readTemplate(controller + '/' + TemplateRendererEngine.LAYOUT_DEFAULT)) == null && controller.indexOf('/') > 0) {
            controller = controller.substring(0, controller.lastIndexOf('/'));
        }
        return template;
    }
    
    private T lookupDefaultLayout() {
        // last resort - this should always be present
        return engine.readTemplate(TemplateRendererEngine.LAYOUT_DEFAULT);
    }
    
    private T cacheTemplate(final String controllerLayoutCombined, final T template, final boolean useCache) {
        if (useCache) {
            cachedTemplates.putIfAbsent(controllerLayoutCombined, template);
        }
        return template;
    }
}
