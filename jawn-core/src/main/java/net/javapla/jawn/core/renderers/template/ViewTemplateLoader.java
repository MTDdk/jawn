package net.javapla.jawn.core.renderers.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.util.AsyncCharArrayWriter;

@Singleton
public class ViewTemplateLoader {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final char[] NOT_FOUND = {'N','F'};
    
    private final DeploymentInfo deploymentInfo;
    private final boolean useCache;
    
    private final HashMap<String, char[]> cachedTemplates = new HashMap<>();
    
    @Inject
    public ViewTemplateLoader(DeploymentInfo info, Modes mode) {
        deploymentInfo = info;
        useCache = mode != Modes.DEV; // use cache if NOT DEV
    }
    
    public static final String getTemplateRootFolder(final DeploymentInfo info) {
        return info.getRealPath(TemplateRendererEngine.TEMPLATES_FOLDER);
    }
    
    public String loadTemplate(final String path) {
        return new String(lookupTemplate(path));
    }
    
    public ViewTemplates load(final View view, final String suffixOfTemplatingEngine) throws Up.ViewError {
        final String templatePath = getTemplatePathForResult(view, suffixOfTemplatingEngine);
        final String layoutName = getLayoutNameForResult(view, suffixOfTemplatingEngine);
        final char[] template = locateContentTemplate(templatePath); 
        
        final Tuple<String, char[]> layout;
        if (layoutName == null) { // no layout
            // both layout and template should never be null
            if (template == null) {
                throw new Up.ViewError("Could not find the template " + templatePath + ". Is it spelled correctly?");
            }
            
            layout = new Tuple<>(null, null);
        } else {
            layout = locateLayoutTemplate(view.path(), layoutName, suffixOfTemplatingEngine/*, useCache*/);
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
            public String template() {
                if (template == null) return new String();
                return new String(template); // actually also does copying of the array
            }
            
            @Override
            public String layout() {
                if (layout.second == null) return new String();
                return new String(layout.second);
            }
        };
    }
    
    public void render(final Context context, ThrowingConsumer<Writer> render, Consumer<Exception> errorOccurred) throws Up.ViewError {
        try (final AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            render.accept(writer);
            
            context.resp().send(writer.toCharBuffer());
        } catch (Exception e) {
            errorOccurred.accept(e);
            throw new Up.ViewError(e);
        }
        //context.resp().outputStream()
    }
    
    public void render(final Context context, ThrowingConsumer<Writer> render) throws Up.ViewError {
        render(context, render, e -> {});
    }
    
    public String renderAsString(ThrowingConsumer<Writer> renderer) throws Up.ViewError {
        try (final AsyncCharArrayWriter writer = new AsyncCharArrayWriter()) {
            renderer.accept(writer);
            
            return writer.toString();
        } catch (Exception e) {
            throw new Up.ViewError(e);
        }
    }

    public final String getLayoutNameForResult(View view, String suffixOfTemplatingEngine) {
        String layout = view.layout();
        if (layout != null) {
//            // handle and cache layout endings
//            layout = layoutCache.computeIfAbsent(layout, (l -> {
//                /*if (l.endsWith(suffixOfTemplatingEngine)) {
//                    int templateSuffixLengthToRemove = suffixOfTemplatingEngine.length() + (suffixOfTemplatingEngine.charAt(0) == '.' ? 0 : 1); // add one if the dot is missing
//                    l = l.substring(0, l.length() - templateSuffixLengthToRemove);
//                }
//                if (!l.endsWith(".html"))
//                    l += ".html";*/
//                if (!l.endsWith(suffixOfTemplatingEngine)) {
//                    l += suffixOfTemplatingEngine;
//                }
//                return l;
//            }));
            
            if (!layout.endsWith(suffixOfTemplatingEngine)) {
                layout += suffixOfTemplatingEngine;
            }
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
        
        return /*StringUtil.blank(path) ? template :*/ path + '/' + template;
        
        
        
        
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
    
    private char[] locateContentTemplate(String templatePath) {
        try {
            return templatePath != null ? lookupTemplate(templatePath) : null;
        } catch (Up.ViewError e) {
            //logger.debug(e.getMessage());
            return null;
        }
    }
    
    /**
     * If found, uses the provided layout within the controller folder.
     * If not found, it looks for the default template within the controller folder
     * then use this to override the root default template
     */
    private Tuple<String,char[]> locateLayoutTemplate(String controller, final String layoutName, String suffixOfTemplatingEngine)/* throws Up.ViewError*/ {
        // first see if we have already looked for the template
        final String controllerLayoutCombined = controller + '/' + layoutName;
        
        try {
            char[] template;
            // look for a layout of a given name within the controller folder
            if ((template = lookupTemplate(controllerLayoutCombined)) != null) // lookupLayoutWithNonDefaultName 
                return new Tuple<>(controllerLayoutCombined, template);
            
            
            // going for defaults
            Tuple<String,char[]> t;
            if ((template = (t = lookupDefaultLayoutInControllerPathChain(controller, suffixOfTemplatingEngine)).second) != null) 
                return t;
            
            if ((template = (t = lookupDefaultLayout(suffixOfTemplatingEngine)).second) != null) 
                return t;
        
        } catch (Up.ViewError e) {
            logger.debug(TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine + " is not to be found anywhere");
        }
        
        return new Tuple<>(controllerLayoutCombined, null);
        //throw new Up.ViewError(TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine + " is not to be found anywhere");
    }
    
    private Tuple<String, char[]/*CharArrayStringReader*/> lookupDefaultLayoutInControllerPathChain(String controller, String suffixOfTemplatingEngine) {
        // look for the default layout in controller folder
        // and bubble up one folder if nothing is found
        // (controller can be: /folder1/folder2/folder3 - so it is possible to bubble up)
        char[] template;
        while ((template = lookupTemplate/*diskReadLayout*/(controller + '/' + TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine)) == null && controller.indexOf('/') > 0) {
            controller = controller.substring(0, controller.lastIndexOf('/'));
        }
        return new Tuple<>(controller + '/' + TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine, template);
    }
    
    private Tuple<String, char[]> lookupDefaultLayout(String suffixOfTemplatingEngine) {
        // last resort - this should always be present
        return new Tuple<>('/' + TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine, lookupTemplate/*diskReadLayout*/(TemplateRendererEngine.LAYOUT_DEFAULT + suffixOfTemplatingEngine));
    }
    
    private char[] lookupTemplate(final String controllerLayoutCombined) {
        if (useCache) {
            char[] arr = cachedTemplates.get(controllerLayoutCombined);
            
            if (arr == null) { // key not found
                
                char[] template = diskReadLayout(controllerLayoutCombined);
                if (template == null) { // file not found
                    cachedTemplates.put(controllerLayoutCombined, NOT_FOUND);
                } else {
                    cachedTemplates.put(controllerLayoutCombined, template);
                }
                return template;
            } else if (arr.length == NOT_FOUND.length && arr[0] == NOT_FOUND[0] && arr[1] == NOT_FOUND[1]) { // previously seen as not found
                //throw new Up.ViewError(controllerLayoutCombined + " is not to be found anywhere");
                logger.debug(controllerLayoutCombined + " is not to be found anywhere");
            }
            return arr;
        }
        // else
        return diskReadLayout(controllerLayoutCombined);
    }
    
    private char[] diskReadLayout(final String controllerLayoutCombined) {
        try (BufferedReader reader = deploymentInfo.viewResourceAsReader(controllerLayoutCombined)) {
            if (useCache) { // no LF
                return new NoNewLineReader(reader).data();
            } else {
                // load the file
                return reader.lines().collect(Collectors.joining("\n")).toCharArray();
                // TODO it seems that this is extremely inefficient, and that one should be able to just read all chars from a file
                // without reading line by line. That would eliminate the "\n" and the .lines()
            }
        } catch (IOException ignore) {
            return null;
        }
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
