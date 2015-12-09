package net.javapla.jawn.core.templates.stringtemplate.rewrite;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.antlr.runtime.ANTLRStringStream;

/**
 * This is purposefully not optimised for speed, as it only ought to be
 * for reading files, and it should be up to other classes to cache the
 * results from this.
 * 
 * @author MTD
 */
public class ANTLRNoNewLineStream extends ANTLRStringStream {
    
    public ANTLRNoNewLineStream(URL f, String encoding) throws IOException {
        
        final InputStreamReader isr;
        if ( encoding!=null ) {
            isr = new InputStreamReader(f.openStream(), encoding);
        } else {
            isr = new InputStreamReader(f.openStream());
        }
        
        // load
        try (IgnoreLFBufferedReader reader = new IgnoreLFBufferedReader(isr)) {
            final StringBuilder bob = new StringBuilder();
            reader.lines().forEach(line -> bob.append(line.trim()));
            
            this.data = bob.toString().toCharArray();
            this.n = bob.length();
        }
    }

}
