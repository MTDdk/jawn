package net.javapla.jawn.core.renderers.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Consumer;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.util.CharArrayList;
import net.javapla.jawn.core.util.CharArrayStringReader;

public class ViewTemplateLoader/*<T>*/ {
    
    private final DeploymentInfo deploymentInfo;
    private final Path realPath;
//    private final TemplateRendererEngine<T> engine;
//    private final String templateSuffix;
//    private final int templateSuffixLengthToRemove;
    private final HashMap<String,String> layoutCache = new HashMap<>();
    
    private final HashMap<String, CharArrayStringReader> cachedTemplates = new HashMap<>();
    
    public ViewTemplateLoader(DeploymentInfo info/*, TemplateRendererEngine<T> engine*/) {
        deploymentInfo = info;
        realPath = getTemplateRootFolder(info);
//        this.engine = engine;
//        templateSuffix = engine.getSuffixOfTemplatingEngine();
//        templateSuffixLengthToRemove = templateSuffix.length() + (templateSuffix.charAt(0) == '.' ? 0 : 1); // add one if the dot is missing
    }
    
    public static final Path getTemplateRootFolder(final DeploymentInfo info) {
        return info.getRealPath(TemplateRendererEngine.TEMPLATES_FOLDER);
    }
    public final Path getTemplateRootFolder() {
        return realPath;
    }
    
    public ViewTemplates/*<T>*/ load(final View view, final String suffixOfTemplatingEngine, final boolean useCache) throws Up.ViewError {
        final String templateName = getTemplateNameForResult(view, suffixOfTemplatingEngine);
        final String layoutName = getLayoutNameForResult(view, suffixOfTemplatingEngine);
        final CharArrayStringReader template = templateName != null ? locateContentTemplate(templateName, useCache) : null; 
        
        final CharArrayStringReader layout;
        if (layoutName == null) { // no layout
            // both layout and template should not be null
            if (template == null) {
                throw new Up.ViewError("Could not find the template " + templateName + ". Is it spelled correctly?");
            }
            
            layout = null;
        } else {
            layout = locateLayoutTemplate(view.path(), layoutName, useCache);
        }
        
        // TODO
        
        return new ViewTemplates/*<T>*/() {
            @Override
            public String templateName() {
                return templateName;
            }

            @Override
            public String layoutName() {
                return layoutName;
            }
            
            @Override
            public Reader templateAsReader() throws FileNotFoundException, IOException {
                if (template == null) throw new FileNotFoundException();
                return template;
            }
            
            @Override
            public Reader layoutAsReader() throws FileNotFoundException, IOException {
                if (layout == null) throw new FileNotFoundException();
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

    public final String getLayoutNameForResult(View view, String suffixOfTemplatingEngine) {
        String layout = view.layout();
        if (layout != null) {
            // handle and cache layout endings
            layout = layoutCache.computeIfAbsent(layout, (l -> {
                if (l.endsWith(suffixOfTemplatingEngine)) {
                    int templateSuffixLengthToRemove = suffixOfTemplatingEngine.length() + (suffixOfTemplatingEngine.charAt(0) == '.' ? 0 : 1); // add one if the dot is missing
                    l = l.substring(0, l.length() - templateSuffixLengthToRemove);
                }
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
    public String getTemplateNameForResult(View view, String suffixOfTemplatingEngine) {
        
        final String path = view.path();
        
        /*
        // Look for a controller named template first
        if (new File(realPath + path + templateSuffix).exists()) {
            return path;
        }*/
        
        String template = view.template();
        if (template.endsWith(suffixOfTemplatingEngine)) {
            int templateSuffixLengthToRemove = suffixOfTemplatingEngine.length() + (suffixOfTemplatingEngine.charAt(0) == '.' ? 0 : 1); // add one if the dot is missing
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
    
    public CharArrayStringReader locateContentTemplate(final String contentTemplateName, boolean useCache) {
        if (useCache && cachedTemplates.containsKey(contentTemplateName)) 
            return cachedTemplates.get(contentTemplateName);
        
        CharArrayStringReader template = lookupLayout/*engine.readTemplate*/(contentTemplateName);
        if (template != null) return cacheTemplate(contentTemplateName, template, useCache);
        
        return null; //throws new ViewException();??
    }
    
    public CharArrayStringReader locateLayoutTemplate(final String controller, final String layout) throws Up.ViewError {
        return locateLayoutTemplate(controller, layout, false);
    }

    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    private CharArrayStringReader locateLayoutTemplate(String controller, final String layout, final boolean useCache) throws Up.ViewError {
        // first see if we have already looked for the template
        final String controllerLayoutCombined = controller + '/' + layout;
        if (useCache && cachedTemplates.containsKey(controllerLayoutCombined)) 
            return cachedTemplates.get(controllerLayoutCombined);//engine.clone(cachedTemplates.get(controllerLayoutCombined)); // README: computeIfAbsent does not save the result if null - which it actually might need to do to minimise disk reads
        
        
        CharArrayStringReader template;
        // look for a layout of a given name within the controller folder
        if ((template = lookupLayout(controllerLayoutCombined)) != null) // lookupLayoutWithNonDefaultName 
            return cacheTemplate(controllerLayoutCombined, template, useCache);
        
        // going for defaults
        if ((template = lookupDefaultLayoutInControllerPathChain(controller)) != null) 
            return cacheTemplate(controllerLayoutCombined, template, useCache);
        
        if ((template = lookupDefaultLayout()) != null) 
            return cacheTemplate(controllerLayoutCombined, template, useCache);
        
        throw new Up.ViewError(TemplateRendererEngine.LAYOUT_DEFAULT /*+ engine.getSuffixOfTemplatingEngine()*/ + " is not to be found anywhere");
    }
    
    private CharArrayStringReader lookupLayout(final String controllerLayoutCombined) {
        try {
            return new CharArrayStringReader(deploymentInfo.resourceAsReader(controllerLayoutCombined));
        } catch (IOException ignore) {
            return null;
        }//engine.readTemplate(controllerLayoutCombined);
    }
    
    private CharArrayStringReader lookupDefaultLayoutInControllerPathChain(String controller) {
        // look for the default layout in controller folder
        // and bubble up one folder if nothing is found
        // (controller can be: /folder1/folder2/folder3 - so it is possible to bubble up)
        CharArrayStringReader template;
        while ((template = lookupLayout/*engine.readTemplate*/(controller + '/' + TemplateRendererEngine.LAYOUT_DEFAULT)) == null && controller.indexOf('/') > 0) {
            controller = controller.substring(0, controller.lastIndexOf('/'));
        }
        return template;
    }
    
    private CharArrayStringReader lookupDefaultLayout() {
        // last resort - this should always be present
        return lookupLayout/*engine.readTemplate*/(TemplateRendererEngine.LAYOUT_DEFAULT);
    }
    
    private CharArrayStringReader cacheTemplate(final String controllerLayoutCombined, final CharArrayStringReader template, final boolean useCache) {
        if (useCache) {
            cachedTemplates.putIfAbsent(controllerLayoutCombined, template);
        }
        return template;
    }
}
