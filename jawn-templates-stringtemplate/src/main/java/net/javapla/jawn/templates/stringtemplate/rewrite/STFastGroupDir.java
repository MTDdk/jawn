package net.javapla.jawn.templates.stringtemplate.rewrite;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Enumeration;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STRawGroupDir;
import org.stringtemplate.v4.compiler.CompiledST;
import org.stringtemplate.v4.misc.ErrorType;
import org.stringtemplate.v4.misc.Misc;

import net.javapla.jawn.core.util.StringUtil;

/**
 * Overriding STRawGroupDir for a shred of greater performance.
 * 
 * @author MTD
 */
public final class STFastGroupDir extends STRawGroupDir {
    
    protected final ArrayList<URL> resourceRoots;
    protected final boolean skipLF;
    public STFastGroupDir(String dirName, char delimiterStartChar, char delimiterStopChar, boolean skipLF) {
        super(dirName, delimiterStartChar, delimiterStopChar);
        this.skipLF = skipLF;
        
        resourceRoots = new ArrayList<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = cl.getResources(dirName);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (!url.getPath().contains("/test/")) {
                    resourceRoots.add(url);
                }
            }
        } catch (IOException e) {
            if ( root == null) {
                cl = this.getClass().getClassLoader();
                root = cl.getResource(dirName);
            }
        }
    }

    @Override
    public final ST getInstanceOf(final String name) {
        CompiledST c = lookupTemplate( ( name.charAt(0)!='/' ) ? "/"+name : name);
        if ( c!=null ) {
            return createStringTemplate(c);
        }
        return null;
    }
    
    @Override
    public final CompiledST lookupTemplate(final String name) {
        CompiledST code = rawGetTemplate(name);
        if ( code == NOT_FOUND_ST ) {
            return null; //  previously seen as not found
        }
        if (code != null) return tryClone(code);
        
        // try to load from disk and look up again
        String unqualifiedName = getFileName(name);//Misc.getFileName(name);
        String prefix = getPrefix(name);//Misc.getPrefix(name);
        
        if ( code == null ) code = loadTemplateFile(prefix, unqualifiedName+TEMPLATE_FILE_EXTENSION); // load t.st file
        if ( code == null ) code = loadTemplateResource(prefix, unqualifiedName+TEMPLATE_FILE_EXTENSION); // load t.st resource
        if ( code == null ) templates.put(name, NOT_FOUND_ST);
        
        //TODO rewrite in order to not use 'templates', which is synchronized
        // we can probably manage with only synchronising writes and not all reads
        
        return code != null ? tryClone(code) : null;
    }
    
    /**
     * Stolen ruthlessly from
     * {@link File#getName()}
     */
    private static final String getFileName(String path) {
        int index = path.lastIndexOf('/');//stop using File.separatorChar as we are not even remotely close to be using that ANYwhere else - apparently. This makes it fail on Windows systems
        int prefixLength = prefixLength(path);
        if (index < prefixLength) return path.substring(prefixLength);
        return path.substring(index + 1);
    }
    private static final String getPrefix(String name) {
        String prefix = Misc.getParent(name);
        if ( !StringUtil.endsWith(prefix, '/') ) return prefix + '/';
        return prefix;
    }

    /**
     * Stolen ruthlessly from
     * {@link FileSystem#prefixLength}
     * 
     * Compute the length of this pathname string's prefix.  The pathname
     * string must be in normal form.
     * 
     * @param pathname Normal form of a file path
     * @return length of this pathname's prefix
     */
    private static final int prefixLength(String pathname) {
        if (pathname.length() == 0) return 0;
        return (pathname.charAt(0) == '/') ? 1 : 0;
    }

    @Override
    public final CompiledST loadTemplateFile(String prefix, String unqualifiedFileName) {
        return loadTemplate(root, prefix, unqualifiedFileName);
    }
    
    /**
     * Loads from a jar or similar resource if the template could not be found
     * directly on the filesystem.
     */
    private final CompiledST loadTemplateResource(String prefix, String unqualifiedFileName) {
        //if (resourceRoot == null) return null;
        //return loadTemplate(resourceRoot, prefix, unqualifiedFileName);
        if (resourceRoots.isEmpty()) return null;
        CompiledST template = null;
        for (URL resourceRoot : resourceRoots) {
            if ((template = loadTemplate(resourceRoot, prefix, unqualifiedFileName)) != null)
                return template;
        }
        return null;
    }
    
    private final CompiledST loadTemplate(URL root, String prefix, String unqualifiedFileName) {
        try {
            
            final URL f = new URL(root + prefix + unqualifiedFileName);
            
            return loadTemplateFile(prefix, unqualifiedFileName, constructStringStream(f));

        } catch (MalformedURLException me) {
            errMgr.runTimeError(null, null, ErrorType.INVALID_TEMPLATE_NAME,
                    me, root + unqualifiedFileName);
        } catch (IOException ioe) {
            if ( verbose ) System.out.println(root+prefix+unqualifiedFileName+" doesn't exist");
            //errMgr.IOError(null, ErrorType.NO_SUCH_TEMPLATE, ioe, unqualifiedFileName);
        }
        
        return null;
    }
    
    private final ANTLRStringStream constructStringStream(URL f) throws IOException {
        return 
            skipLF ? 
            new ANTLRNoNewLineStream(f, encoding) : //removing \r and \n and trimming lines
            new ANTLRInputStream(f.openStream(), encoding); //reading templates as is
    }
    
    private final CompiledST tryClone(CompiledST template) {
        try {
            return template.clone();
        } catch (CloneNotSupportedException e) {
            return template;
        }
    }
}
