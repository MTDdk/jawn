package net.javapla.jawn.template.stringtemplate.rewrite;

import java.io.IOException;
import java.io.Writer;

import org.stringtemplate.v4.AutoIndentWriter;

public class FastAutoIndentWriter extends AutoIndentWriter {

    public FastAutoIndentWriter(Writer out) {
        super(out, "\n");
    }
    
    @Override
    public int write(String str) throws IOException {
        int n = 0;
        int nll = 1; //'\n' //newline.length();
        int sl = str.length();
        
        char[] buffer = new char[sl]; int b = 0;
        
        for (int i=0; i<sl; i++) {
            char c = str.charAt(i);
            // found \n or \r\n newline?
            if ( c=='\r' ) continue;
            if ( c=='\n' ) {
                atStartOfLine = true;
                charPosition = -nll; // set so the write below sets to 0
                //out.write(newline);
                buffer[b++] = '\n';
                n += nll;
                charIndex += nll;
                charPosition += n; // wrote n more char
                continue;
            }
            // normal character
            // check to see if we are at the start of a line; need indent if so
            if ( atStartOfLine ) {
                n+=indent();
                atStartOfLine = false;
            }
            n++;
            //out.write(c);
            buffer[b++] = c;
            charPosition++;
            charIndex++;
        }
        out.write(buffer, 0, b);
        return n;
    }
 /* CharBuffer
14:12:34.567 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 47ms
14:12:40.379 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 13ms
14:12:42.038 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 12ms
14:12:43.757 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 10ms
14:15:00.910 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 7ms
14:15:01.465 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 12ms
14:15:01.875 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
14:15:02.195 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 6ms
14:15:02.559 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 3ms
14:15:02.899 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 4ms
14:15:03.212 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 3ms
14:15:03.541 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 3ms
14:15:03.961 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
  */
    /*StringBuilder
14:17:16.777 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 48ms
14:17:17.403 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 13ms
14:17:17.802 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 11ms
14:17:18.065 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 6ms
14:17:18.308 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
14:17:18.420 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
14:17:18.569 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 8ms
14:17:18.745 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 4ms
14:17:18.932 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 7ms
14:17:19.122 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 8ms
14:17:19.310 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 7ms
14:17:19.500 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
14:17:19.638 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 3ms
     */
    /* char[]
14:44:30.662 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 57ms
14:44:31.132 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 8ms
14:44:31.556 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 6ms
14:44:31.921 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
14:44:32.170 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 6ms
14:44:32.405 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 4ms
14:44:32.645 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 6ms
14:44:32.766 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 4ms
14:44:32.951 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
14:44:33.251 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 5ms
14:44:33.367 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 3ms
14:44:33.663 [1 task-2] DEBUG StringTemplateTemplateRenderer - Rendered template index.html in 3ms
     */
}
