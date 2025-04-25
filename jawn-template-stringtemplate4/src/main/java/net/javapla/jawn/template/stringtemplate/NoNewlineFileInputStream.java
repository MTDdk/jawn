package net.javapla.jawn.template.stringtemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class NoNewlineFileInputStream extends FileInputStream {

    public NoNewlineFileInputStream(String filePath) throws FileNotFoundException {
        super(filePath);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = 0, c;
        do {
            c = this.read();
            if(c != -1) {
                b[off + n] = (byte) c;
                n++;
                len--;  
            } else {
                return c;
            }
        } while(c != -1 && len > 0);
        return n;
    }
    
    @Override
    public int read() throws IOException {
        int c;
        do {
            c = super.read();
        } while(c != -1 && (c == '\n' || c == '\r'));
        return c;
    }
}
