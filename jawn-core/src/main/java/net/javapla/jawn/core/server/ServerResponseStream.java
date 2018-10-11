package net.javapla.jawn.core.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import net.javapla.jawn.core.http.Response;
import net.javapla.jawn.core.http.ResponseStream;

public class ServerResponseStream implements ResponseStream {
    
    private final Response response;
    private ManagedOutputStream stream;
    private ManagedWriter writer;

    ServerResponseStream(Response response) {
        this.response = response;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return (stream = new ManagedOutputStream(response.outputStream()));
    }

    @Override
    public Writer getWriter() throws IOException {
        return (writer = new ManagedWriter(response.writer()));
    }

    @Override
    public void close() throws IOException {
        try {
            if (writer != null)
                writer.managedClose();
            else if (stream != null)
                stream.managedClose();
        } finally {
            response.end();
        }
    }
    
    private class ManagedOutputStream extends OutputStream {
        
        private final OutputStream original;
        
        ManagedOutputStream(OutputStream original) {
            this.original = original;
        }
        
        @Override
        public void close() throws IOException {
            // NO OP
            // Do not let the user be able to close this stream
            // It should be up to the framework to handle it
        }
        
        void managedClose() throws IOException {
            try {
                original.flush();
            } catch(IOException e) {
                // The stream might already have been closed
                // (most likely due to a reload of the browser)
            } finally {
                original.close();
            }
        }

        @Override
        public void write(int b) throws IOException {
            original.write(b);
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            original.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            original.write(b, off, len);
        }
        
        @Override
        public void flush() throws IOException {
            /*if (!response.committed())
                original.flush();*/
        }
    }
    
    private class ManagedWriter extends Writer {
        
        private final Writer original;

        ManagedWriter(Writer original) {
            this.original = original;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            original.write(cbuf, off, len);
        }
        
        @Override
        public Writer append(char c) throws IOException {
            return original.append(c);
        }
        
        @Override
        public Writer append(CharSequence csq) throws IOException {
            return original.append(csq);
        }
        
        @Override
        public Writer append(CharSequence csq, int start, int end) throws IOException {
            return original.append(csq, start, end);
        }
        
        @Override
        public void write(char[] cbuf) throws IOException {
            original.write(cbuf);
        }
        
        @Override
        public void write(int c) throws IOException {
            original.write(c);
        }
        
        @Override
        public void write(String str) throws IOException {
            original.write(str);
        }
        
        @Override
        public void write(String str, int off, int len) throws IOException {
            original.write(str, off, len);
        }

        @Override
        public void flush() throws IOException {
            /*if (!response.committed())
                original.flush();*/
        }

        @Override
        public void close() throws IOException {
            // NO OP
            // Do not let the user be able to close this stream
            // It should be up to the framework to handle it
        }
        
        public void managedClose() throws IOException {
            original.flush();
            original.close();
        }
        
    }
    
}
