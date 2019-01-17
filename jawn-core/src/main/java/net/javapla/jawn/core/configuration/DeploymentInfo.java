package net.javapla.jawn.core.configuration;

import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;

public class DeploymentInfo {
    
    private final String WEBAPP_FOLDER_NAME = "webapp/";

	private final String webappPath;
	private final String contextPath;
	private final boolean isContextPathSet;
	private final int contextPathLength;
	
	public DeploymentInfo(final JawnConfigurations properties, final String contextPath) {
		webappPath = assertPath(properties.getSecure(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH).orElse("")) + WEBAPP_FOLDER_NAME;
		this.contextPath = contextPath;
		isContextPathSet = !contextPath.isEmpty();
		contextPathLength = contextPath.length();
	}
	
	private static String assertPath(String path) {
		if (!StringUtil.blank(path) && !path.endsWith("/"))
			return path + "/";
		return path;
	}
	
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
    	if (isContextPathSet && path.startsWith(contextPath)) return webappPath + path.substring(contextPath.length());
        return webappPath + path;
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
        return stripContextPath(contextPath, contextPathLength, path);
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
