package net.javapla.jawn.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;

import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;

public class DeploymentInfo {
    
    private final String WEBAPP_FOLDER_NAME = "webapp";
    private final String WEBAPP_TEMPLATES_FOLDER_NAME = "views";

	private final String webappPath;
	private final String viewsPath;
	private final String contextPath;
	private final boolean isContextPathSet;
	private final int contextPathLength;
	private final Charset charset;
	
	private final ArrayList<URL> resourceRoots;

	
	public DeploymentInfo(final Config conf, final Charset charset, final String contextPath) {
        final String wp = conf.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH).orElse(WEBAPP_FOLDER_NAME);
        final String views = conf.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_TEMPLATES_PATH).orElse(WEBAPP_TEMPLATES_FOLDER_NAME);
	    
		this.webappPath = assertNoEndSlash(wp); // is allowed to have start slash, e.g.: /var/www/webapp
		this.viewsPath = /*webappPath + */assertStartSlash(assertNoEndSlash(views));
		this.contextPath = assertStartSlash(assertNoEndSlash(contextPath));
		this.isContextPathSet = !this.contextPath.isEmpty();
		this.contextPathLength = this.contextPath.length();
		
		this.charset = charset;
		
		this.resourceRoots = new ArrayList<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = cl.getResources(WEBAPP_FOLDER_NAME); // should this be "webappPath" or fixed to always point at framework internal resources?
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (!url.getPath().contains("/test/") && !url.getPath().contains("/default/")) {
                    resourceRoots.add(url);
                }
            }
        } catch (IOException ignore) { }
	}
	
	public void addResourceRoot(URL resourceRoot) {
	    resourceRoots.add(resourceRoot);
	}
	
	private static String assertStartSlash(final String path) {
	    if (!StringUtil.blank(path) && !StringUtil.startsWith(path, '/')) 
	        return '/' + path;
        return path;
	}
	
	private static String assertNoEndSlash(final String path) {
	    if (!StringUtil.blank(path) && StringUtil.endsWith(path, '/')) 
            return path.substring(0, path.length() -1);
        return path;
	}
	
	/*private static String assertEndSlash(final String path) {
	    if (!StringUtil.blank(path) && !StringUtil.endsWith(path, '/')) 
	        return path + '/';
        return path;
	}
	
	private static String assertNoStartSlash(final String path) {
	    if (!StringUtil.blank(path) && StringUtil.startsWith(path, '/'))
	        return path.substring(1);
	    return path;
	}*/
	
	/**
     * Returns a String containing the real path for a given virtual path. For example, the path "/index.html" returns
     * the absolute file path on the server's filesystem would be served by a request for
     * "http://host/contextPath/index.html", where contextPath is the context path of this ServletContext.
     * 
     * <p>The real path returned will be in a form appropriate to the computer and operating system on which the servlet
     * container is running, including the proper path separators. This method returns null if the servlet container
     * cannot translate the virtual path to a real path for any reason (such as when the content is being made
     * available from a .war archive).</p>
     *
     * <p>
     * JavaDoc copied from: <a href="http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29">
     * http://download.oracle.com/javaee/1.3/api/javax/servlet/ServletContext.html#getRealPath%28java.lang.String%29</a>
     * </p>
     *
     * @param path a String specifying a virtual path
     * @return a String specifying the real path, or null if the translation cannot be performed
     */
    public String getRealPath(final String path) {
        
        
    	if (path == null) return null;
    	if (path.startsWith(webappPath)) return path;

    	
    	String p = assertStartSlash(path);
    	
    	// if there is a contextPath and path starts with contextPath, remove the contextPath
        if (isContextPathSet && p.startsWith(contextPath)) 
            p = p.substring(contextPath.length());

        
        return Paths.get(webappPath + p).toAbsolutePath().toString();
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public String translateIntoContextPath(String path) {
        if (!isContextPathSet) 
            return path;
        if (path.charAt(0) == '/')
            return contextPath + path;
        return contextPath + '/' + path;
    }
    
    public void translateIntoContextPath(String[] paths) {
        if (isContextPathSet) {
            for (int i = 0; i < paths.length; i++) {
                paths[i] = translateIntoContextPath(paths[i]);
            }
        }
    }
    
    public String stripContextPath(final String path) {
        if (!isContextPathSet) return path;
        
        // Cache the result when in PROD?
        return stripContextPath(contextPath, contextPathLength, path);
    }
    
    public InputStream resourceAsStream(final String path) throws NoSuchFileException {
        final String real = getRealPath(path);
        
        // Read from file system
        final File f = new File(real);
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException e) { }
        
        
        // Else try to read from resources
        try {
            return resourceURL(path).openStream();
        } catch (IOException ignore) { }
        
        // Perhaps mark the resource as not found for later lookup
        
        
        throw new NoSuchFileException(real);
    }
    
    private URL resourceURL(final String path) throws NoSuchFileException {
        String p = assertStartSlash(path);
        if (!resourceRoots.isEmpty()) {
            for (URL resourceRoot : resourceRoots) {
                try {
                    return new URL(resourceRoot, WEBAPP_FOLDER_NAME + p);
                } catch (IOException e) { }
            }
        }
        
        throw new NoSuchFileException(p);
    }
    
    public BufferedReader resourceAsReader(final String path) throws NoSuchFileException {
        return new BufferedReader(new InputStreamReader(resourceAsStream(path), charset));
    }
    
    // Does not handle contextPath
    public BufferedReader viewResourceAsReader(final String path) throws NoSuchFileException {
        String p = assertStartSlash(path);
        return resourceAsReader(p.startsWith(viewsPath) ? p : viewsPath + p);
        
        //return p.startsWith(viewsPath) ? Files.newBufferedReader(Paths.get(p), charset) : Files.newBufferedReader(Paths.get(viewsPath, p), charset);
    }
    
    public boolean resourceExists(final String path) {
        try (InputStream i = resourceAsStream(path)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public long resourceLastModified(final String path) {
        final String real = getRealPath(path);
        
        // Read from file system
        File f = file(real);
        if (f.exists()) return f.lastModified();
        
        
        // Else try to read from resources
        try {
            URL url = resourceURL(path);

            File file = new File(url.getPath());
            if (file.canRead()) {
                return file.lastModified();
            }

            // FileUrlConnection opens an inputstream on #getLastModified, whereas JarURLConnection does not
            // So we do not use #openConnection when we know/assume the resource to be a simple file on the filesystem
            long l = url.openConnection().getLastModified();
            if (l > 0) return l;
        } catch (IOException ignore) { }
        
        return -1;
    }
    
    private File file(String realPath) {
        return new File(realPath);
    }
    
    public static final String stripContextPath(final String contextPath, final String requestPath) {
        return stripContextPath(contextPath, contextPath.length(), requestPath);
    }
    
    public static final String stripContextPath(final String contextPath, final int contextPathLength, final String requestPath) {
        if (contextPath.isEmpty() || requestPath.length() <= contextPathLength) return requestPath;
        
        // remove from beginning
        for (int c = 0; c < contextPathLength; c++) {
            if (contextPath.charAt(c) != requestPath.charAt(c)) return requestPath;
        }
        
        return requestPath.substring(contextPathLength);
    }
}
