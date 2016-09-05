package net.javapla.jawn.core.templates.stringtemplate.rewrite;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;

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
 *
 */
public final class STFastGroupDir extends STRawGroupDir {
    
    protected final boolean skipLF;
    public STFastGroupDir(String dirName, char delimiterStartChar, char delimiterStopChar, boolean skipLF) {
        super(dirName, delimiterStartChar, delimiterStopChar);
        this.skipLF = skipLF;
    }

    @Override
    public final ST getInstanceOf(String name) {
        if ( name.charAt(0)!='/' ) name = "/"+name;
        CompiledST c = lookupTemplate(name);
        if ( c!=null ) {
            return createStringTemplate(c);
        }
        return null;
    }
    
    @Override
    public final CompiledST lookupTemplate(final String name) {
        CompiledST code = rawGetTemplate(name);
        if ( code==NOT_FOUND_ST ) {
            if ( verbose ) System.out.println(name+" previously seen as not found");
            return null;
        }
        // try to load from disk and look up again
        if ( code==null ) code = load(name);
        //if ( code==null ) code = lookupImportedTemplate(name);
        if ( code==null ) templates.put(name, NOT_FOUND_ST);
        
        //TODO rewrite in order to not use 'templates', which is synchronized
        // we can probably manage with only synchronising writes and not all reads
        
        return code;
    }
    
    @Override
    protected final CompiledST load(String name) {
        String unqualifiedName = getFileName(name);//Misc.getFileName(name);
        String prefix = getPrefix(name);//Misc.getPrefix(name);
        return loadTemplateFile(prefix, unqualifiedName+TEMPLATE_FILE_EXTENSION); // load t.st file
    }
    
    /**
     * Stolen ruthlessly from
     * {@link File#getName()}
     * @param path
     * @return
     */
    private static final String getFileName(String path) {
        int index = path.lastIndexOf('/');//stop using File.separatorChar as we are not even remotely close to be using that ANYwhere else - apparently. This makes it fail on Windows systems
        int prefixLength = prefixLength(path);
        if (index < prefixLength) return path.substring(prefixLength);
        return path.substring(index + 1);
    }
    private static final String getPrefix(String name) {
        String parent = Misc.getParent(name);
        String prefix = parent;
        if ( !StringUtil.endsWith(parent, '/') ) prefix += '/';
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
        try {
            final URL f = new URL(root+prefix+unqualifiedFileName);

            final ANTLRStringStream fs = 
                    skipLF ? 
                    new ANTLRNoNewLineStream(f, encoding) : //removing \r and \n and trimming lines
                    new ANTLRInputStream(f.openStream(), encoding); //reading templates as is
                    //TODO adding a HTML character converter here
                    
            //fs.name = unqualifiedFileName; //most likely extremely unnecessary

            return loadTemplateFile(prefix, unqualifiedFileName, fs);

        } catch (MalformedURLException me) {
            errMgr.runTimeError(null, null, ErrorType.INVALID_TEMPLATE_NAME,
                    me, root + unqualifiedFileName);
        } catch (IOException ioe) {
            if ( verbose ) System.out.println(root+"/"+unqualifiedFileName+" doesn't exist");
            //errMgr.IOError(null, ErrorType.NO_SUCH_TEMPLATE, ioe, unqualifiedFileName);
        }
        
        return null;
    }
    
}
