/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/

package net.javapla.jawn.trash;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class to discover controller packages under "app.controllers" package in Jar files and directories
 * on classpath.
 *
 * @author Igor Polevoy
 */
@Deprecated
class ControllerPackageLocator {

    private static Logger logger = LoggerFactory.getLogger(ControllerPackageLocator.class);


    public static List<String> locateControllerPackages(ServletContext context) {
        String controllerPath = /*System.getProperty("file.separator") +*/ Configuration.getRootPackage() + System.getProperty("file.separator") + "controllers";
        List<String> controllerPackages = new ArrayList<String>();
        List<URL> urls = getUrls(controllerPath, context);
        for (URL url : urls) {
            File f = new File(url.getFile());
            if (f.isDirectory()) {
                try {
                    discoverInDirectory(f.getCanonicalPath() /*+ controllerPath*/, controllerPackages, "");
                } catch (Exception ignore) { }
            } else {//assuming jar file
                discoverInJar(f, controllerPackages);
            }
        }
        return controllerPackages;
    }

    private static void discoverInDirectory(String directoryPath, List<String> controllerPackages, String parent) {
        try {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                //nothing
            } else {
                File[] files = directory.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        controllerPackages.add(parent + (parent.equals("") ? "" : ".") + file.getName());
                        discoverInDirectory(file.getCanonicalPath(), controllerPackages, parent + (parent.equals("") ? "" : ".") + file.getName());
                    }
                }
            }
        } catch (Exception ignore) {
        }
    }

    protected static void discoverInJar(File file, List<String> controllerPackages) {
        String base = "app/controllers/";
        try (JarFile jarFile = new JarFile(file)) {

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                String path = jarEntry.toString();
                if (path.startsWith(base) && !path.endsWith(".class") && !path.equals(base)) {
                    controllerPackages.add(path.substring(base.length(), path.length() - 1).replace("/", "."));
                }
            }
        } catch (Exception ignore) {}
    }

    private static List<URL> getUrls(String controllerPath, ServletContext context) {
        try {
//        URL[] urls;
//            urls = //((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
//            return Arrays.asList(urls);
            
            //MTD: it kept failing finding any urls at all. Not even a simple empty url "".
            //So the measures below seems to do the trick.
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(controllerPath);
            List<URL> urls = new ArrayList<>();
            while (resources.hasMoreElements())
                urls.add(resources.nextElement());
            return urls;
        } catch (ClassCastException | IOException e) {
            return hackForWeblogic(context);
        }
    }

    //Maybe this is a hack for other containers too?? Maybe this is not a hack at all?
    private static List<URL> hackForWeblogic(ServletContext context) {
        List<URL> urls = new ArrayList<URL>();
        Set<String> libJars = context.getResourcePaths("/WEB-INF/lib");
        for (String jar : libJars) {
            try {
                urls.add(context.getResource((String) jar));
            }
            catch (MalformedURLException e) {
                logger.warn("Failed to get resource: " + jar);
            }
        }
        addClassesUrl(context, urls);
        return urls;
    }

    private static void addClassesUrl(ServletContext context, List<URL> urls) {
        Set<String> resources = context.getResourcePaths("/WEB-INF/classes/");
        if (!resources.isEmpty()) {
            try {
                String first = resources.iterator().next();
                String urlString = context.getResource(first).toString();
                //example:
                // zip:/home/igor/projects/domains/vrs/domain/vrs/servers/vrs_web/tmp/_WL_user/_appsdir_vrs-ear-4.0.1-SNAPSHOT_ear/tr9ese/war/WEB-INF/lib/_wl_cls_gen.jar!/com/
                String url = urlString.substring(urlString.indexOf(":") + 2, urlString.indexOf("!"));
                urls.add(new URL("file:/" + url));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
