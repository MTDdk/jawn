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
            if (ze.isDirectory()) continue;

            final String classname = ze.getName();
            if (classname.startsWith(SCANNED_PATH)/* && classname.endsWith(ControllerFinder.CONTROLLER_CLASS_SUFFIX)*/) {
                try {
                    URL url = new URL(String.format("jar:file:%s!/%s", JAR_FILE, classname));
                    retval.add(url);
                } catch (MalformedURLException e1) {
                    throw new Error(e1);
                }
            }
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
