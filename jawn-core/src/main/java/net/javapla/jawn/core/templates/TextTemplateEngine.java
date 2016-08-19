package net.javapla.jawn.core.templates;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

@Singleton
final class TextTemplateEngine implements TemplateEngine {

    @Override
    public final void invoke(final Context context, final Response response, final ResponseStream stream) {
        Object obj = response.renderable();
        if (obj == null) return;
        
        try {
            
            if (obj instanceof String) {
                try (Writer output = stream.getWriter()) {
                    output.write((String)obj);
                }
            } else if (obj instanceof byte[]) {
                try (OutputStream output = stream.getOutputStream()) {
                    output.write((byte[]) obj);
                }
            } else if (obj instanceof char[]) {
                try (Writer output = stream.getWriter()) {
                    output.write((char[]) obj);
                }
            } else {
                try (Writer output = stream.getWriter()) {
                    output.write(obj.toString());
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        // intentionally null
        return null;
    }

    @Override
    public String[] getContentType() {
        return new String[]{MediaType.TEXT_PLAIN};
    }
    /*@Override
    public ContentType[] getContentType2() {
        return new ContentType[]{ContentType.TEXT_PLAIN};
    }*/

}
