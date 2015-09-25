package net.javapla.jawn.core.reflection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

public class JarLoader implements AutoCloseable {
    
    private final String SCANNED_PATH;
    private final String JAR_FILE;
    private final JarFile zf;
    
    public JarLoader(final String scannedPath) throws ZipException, IOException {
        SCANNED_PATH = scannedPath;
        
        String jarFile = Thread.currentThread().getContextClassLoader().getResource(scannedPath).getFile();
        JAR_FILE = extractJarFileFromClassPathFilename(jarFile);
        
        zf = new JarFile(JAR_FILE);
    }

    public Enumeration<URL> getResourcesFromJarFile() {
            final List<URL> retval = new ArrayList<>();
            
            final Enumeration<JarEntry> e = zf.entries();
            
            while(e.hasMoreElements()){
                final JarEntry ze = (JarEntry) e.nextElement();
                final String classname = ze.getName();
                if (classname.startsWith(SCANNED_PATH)/* && classname.endsWith(ControllerFinder.CONTROLLER_CLASS_SUFFIX)*/) {
                    /*try {
                            Class.forName(fileName.substring(0, fileName.length() - CLASS_SUFFIX.length()).replace(SLASH, DOT));
    //                            Class.forName("mtd.ibi.server.services.IndexController");
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        }*/
                    try {
                        retval.add(new URL(String.format("jar:file:%s!/%s", JAR_FILE, classname)));
                    } catch (MalformedURLException e1) {
                        throw new Error(e1);
                    }
                }
    //                final boolean accept = pattern.matcher(fileName).matches();
                /*if(accept){
                    retval.add(fileName);
                }*/
            }
            
            return Collections.enumeration(retval);
        }

    @Override
    public void close() throws IOException {
        zf.close();
    }

    private static String extractJarFileFromClassPathFilename(String file) {
        return file.substring("file:".length(), file.indexOf('!'));
    }

}
