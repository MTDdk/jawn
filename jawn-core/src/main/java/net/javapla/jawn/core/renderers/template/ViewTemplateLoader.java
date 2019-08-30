package net.javapla.jawn.core.renderers.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Consumer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.util.CharArrayList;
import net.javapla.jawn.core.util.CharArrayStringReader;
import net.javapla.jawn.core.util.StringUtil;

@Singleton
public class ViewTemplateLoader {
    
    private final DeploymentInfo deploymentInfo;
    private final Path realPath; // TODO to be deprecated
    //private final HashMap<String,String> layoutCache = new HashMap<>();
    
    private final HashMap<String, CharArrayStringReader> cachedTemplates = new HashMap<>();
    
    @Inject
    public ViewTemplateLoader(DeploymentInfo info) {
        deploymentInfo = info;
        realPath = getTemplateRootFolder(info);
    }
    
    public static final Path getTemplateRootFolder(final DeploymentInfo info) {
        return info.getRealPath(TemplateRendererEngine.TEMPLATES_FOLDER);
    }
    public final Path getTemplateRootFolder() {
        return realPath;
    }
    
    public ViewTemplates load(final View view, final String suffixOfTemplatingEngine, final boolean useCache) throws Up.ViewError {
        final String templatePath = getTemplatePathForResult(view, suffixOfTemplatingEngine);
        final String layoutName = getLayoutNameForResult(view, suffixOfTemplatingEngine);
        final CharArrayStringReader template = templatePath != null ? locateContentTemplate(templatePath, useCache) : null; 
        
        final Tuple<String, CharArrayStringReader> layout;
        if (layoutName == null) { // no layout
            // both layout and template should not be null
            if (template == null) {
                throw new Up.ViewError("Could not find the template " + templatePath + ". Is it spelled correctly?");
            }
            
            layout = new Tuple<>(layoutName, null);
        } else {
            layout = locateLayoutTemplate(view.path(), layoutName, suffixOfTemplatingEngine, useCache);
        }
        
        return new ViewTemplates() {
            @Override
            public String templatePath() {
                return templatePath;
            }

            @Override
            public String layoutPath() {
                return layout.first;//layoutName;
            }
            
            @Override
            public boolean templateFound() {
                return template != null;
            }
            
            @Override
            public boolean layoutFound() {
                return layout.second != null;
            }
            
            @Override
            public Reader templateAsReader() throws FileNotFoundException, IOException {
                if (template == null) throw new FileNotFoundException();
                return template;
            }
            
            @Override
            public Reader layoutAsReader() throws FileNotFoundException, IOException {
                if (layout.second == null) throw new FileNotFoundException();
                return layout.second;
            }
        };
    }
    
    public void render(final Context context, ThrowingConsumer<Writer> render, Consumer<Exception> errorOccurred) throws Up.ViewError {
        try (final CharArrayList writer = new CharArrayList(8192)/*stream.getWriter()*/) {
            render.accept(writer);
            
            context.resp().send(writer.toCharBuffer());
        } catch (Exception e) {
            errorOccurred.accept(e);
            throw new Up.ViewError(e);
        }
    }
    
    public void render(final Context context, ThrowingConsumer<Writer> render) throws Up.ViewError {
        render(context, render, e -> {});
    }

    public final String getLayoutNameForResult(View view, String suffixOfTemplatingEngine) {
        String layout = view.layout();
//        if (layout != null) {
//            // handle and cache layout endings
//            layout = layoutCache.computeIfAbsent(layout, (l -> {
//                /*if (l.endsWith(suffixOfTemplatingEngine)) {
//                    int templateSuffixLengthToRemove = suffixOfTemplatingEngine.length() + (suffixOfTemplatingEngine.charAt(0) == '.' ? 0 : 1); // add one if the dot is missing
//                    l = l.substring(0, l.length() - templateSuffixLengthToRemove);
//                }
//                if (!l.endsWith(".html"))
//                    l += ".html";*///TODO .html needs to be a part
//                if (!l.endsWith(suffixOfTemplatingEngine)) {
//                    l += suffixOfTemplatingEngine;
//                }
//                return l;
//            }));
//        }
        
        if (!layout.endsWith(suffixOfTemplatingEngine)) {
          layout += suffixOfTemplatingEngine;
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
    public String getTemplatePathForResult(View view, String suffixOfTemplatingEngine) {
        
        final String path = view.path();
        
        /*
        // Look for a controller named template first
        if (new File(realPath + path + templateSuffix).exists()) {
            return path;
        }*/
        
        String template = view.template();
        if (!template.endsWith(suffixOfTemplatingEngine)) {
            template = template + suffixOfTemplatingEngine;
        }
        
        return StringUtil.blank(path) ? template : path + '/' + template;
        
        
        
        
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
        
        CharArrayStringReader template = lookupLayout(contentTemplateName);
        if (template != null) return cacheTemplate(contentTemplateName, template, useCache);
        
        return null; //throws new ViewException();??
    }
    
    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    private Tuple<String,CharArrayStringReader> locateLayoutTemplate(String controller, final String layoutName, String suffixOfTemplatingEngine, final boolean useCache) throws Up.ViewError {
        // first see if we have already looked for the template
        final String controllerLayoutCombined = StringUtil.blank(controller) ? layoutName : controller + '/' + layoutName;
        if (useCache && cachedTemplates.containsKey(controllerLayoutCombined)) 
            return new Tuple<>(controllerLayoutCombined, cachedTemplates.get(controllerLayoutCombined)); // README: computeIfAbsent does not save the result if null - which it actually might need to do to minimise disk reads
        
        CharArrayStringReader template;
        // look for a layout of a given name within the controller folder
        if ((template = lookupLayout(controllerLayoutCombined)) != null) // lookupLayoutWithNonDefaultName 
            return new Tuple<>(controllerLayoutCombined, cacheTemplate(controllerLayoutCombined, template, useCache));
        
        // going for defaults
        Tuple<String,CharArrayStringReader> t;
        if ((template = (t = lookupDefaultLayoutInControllerPathChain(controller, suffixOfTemplatingEngine)).second) != null) 
            return new Tuple<>(t.first, cacheTemplate(controllerLayoutCombined, template, useCache));
        
        if ((template = (t = lookupDefaultLayout(suffixOfTemplatingEngine)).second) != null) 
            return new Tuple<>(t.first, cacheTemplate(controllerLayoutCombined, template, useCache));
        
        throw new Up.ViewError(TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine + " is not to be found anywhere");
    }
    
    private CharArrayStringReader lookupLayout(final String controllerLayoutCombined) {
        try {
            return new CharArrayStringReader(deploymentInfo.viewResourceAsReader(controllerLayoutCombined));
        } catch (IOException ignore) {
            return null;
        }
    }
    
    private Tuple<String, CharArrayStringReader> lookupDefaultLayoutInControllerPathChain(String controller, String suffixOfTemplatingEngine) {
        // look for the default layout in controller folder
        // and bubble up one folder if nothing is found
        // (controller can be: /folder1/folder2/folder3 - so it is possible to bubble up)
        CharArrayStringReader template;
        while ((template = lookupLayout(controller + '/' + TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine)) == null && controller.indexOf('/') > 0) {
            controller = controller.substring(0, controller.lastIndexOf('/'));
        }
        return new Tuple<>(controller + '/' + TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine, template);
    }
    
    private Tuple<String, CharArrayStringReader> lookupDefaultLayout(String suffixOfTemplatingEngine) {
        // last resort - this should always be present
        return new Tuple<>(TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine, lookupLayout(TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine));
    }
    
    private CharArrayStringReader cacheTemplate(final String controllerLayoutCombined, final CharArrayStringReader template, final boolean useCache) {
        if (useCache) {
            cachedTemplates.putIfAbsent(controllerLayoutCombined, template);
        }
        return template;
    }
    
    private class Tuple<T,U> {
        final T first;
        final U second;
        Tuple(T f, U s) { first = f; second = s; }
    }
    
    // TODO does this not already exists?
    @FunctionalInterface
    public static interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }
}
