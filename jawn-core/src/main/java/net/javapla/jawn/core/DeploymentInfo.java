package net.javapla.jawn.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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

	
	public DeploymentInfo(final Config conf, final Charset charset, final String contextPath) {
        final String wp = conf.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH).orElse(WEBAPP_FOLDER_NAME);
        final String views = conf.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_TEMPLATES_PATH).orElse(WEBAPP_TEMPLATES_FOLDER_NAME);
	    
		this.webappPath = assertNoEndSlash(wp); // is allowed to have start slash, e.g.: /var/www/webapp
		this.viewsPath = webappPath + assertStartSlash(assertNoEndSlash(views));
		this.contextPath = assertStartSlash(assertNoEndSlash(contextPath));
		this.isContextPathSet = !this.contextPath.isEmpty();
		this.contextPathLength = this.contextPath.length();
		
		this.charset = charset;
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
    public Path getRealPath(final String path) {
    	if (path == null) return null;
    	
    	final String p = assertStartSlash(path); 
    	
    	// if there is a contextPath and it starts with contextPath, remove the contextPath
    	if (isContextPathSet && p.startsWith(contextPath)) return Paths.get(webappPath + p.substring(contextPath.length()));
        return Paths.get(webappPath + p);
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
    
    // TODO: This does only take the file system into account
    // In order to be more powerful, it should also look for other resources with Thread.currentThread().getContextClassLoader().getResources(dir..)
    // Just like STFastGroupDir of jawn-templates-stringtemplate
    public InputStream resourceAsStream(final String path) throws IOException {
        Path p = getRealPath(path);
        return Files.newInputStream(p, StandardOpenOption.READ);
    }
    
    public BufferedReader resourceAsReader(final String path) throws IOException {
        Path p = getRealPath(path);
        return Files.newBufferedReader(p, charset);
    }
    
    // Does not handle contextPath
    public BufferedReader viewResourceAsReader(final String path) throws IOException {
        String p = assertStartSlash(path);
        return p.startsWith(viewsPath) ? Files.newBufferedReader(Paths.get(p), charset) : Files.newBufferedReader(Paths.get(viewsPath, p), charset);
    }
    
    public boolean resourceExists(final String path) {
        return Files.exists(getRealPath(path));
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
