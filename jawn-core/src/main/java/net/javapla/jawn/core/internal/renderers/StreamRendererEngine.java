package net.javapla.jawn.core.internal.renderers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.renderers.RendererEngine;

@Singleton
class StreamRendererEngine implements RendererEngine {

    @Override
    public void invoke(final Context context, final Object obj) throws Exception {
        
        if (obj instanceof InputStream) {
            //context.resp().send((InputStream) obj);
            try (InputStream s = (InputStream) obj) {
                //context.resp().send(s);
                ((InputStream) obj).transferTo(context.resp().outputStream());
                //context.resp().outputStream().close();
            } catch (IOException e) {
                context.resp().outputStream().close();
            }
        } else if (obj instanceof byte[]) {
            context.resp().send((byte[]) obj);
        } else if (obj instanceof File) {
            try (FileInputStream stream = new FileInputStream((File) obj)) {
                //context.resp().send(stream);
                ((InputStream) stream).transferTo(context.resp().outputStream());
            } catch (IOException e) {
                context.resp().outputStream().close();
            }
        } else {
            context.resp().send( obj.toString() );
        }
        /*} else if (obj instanceof Serializable) {*/
        
        // 'instanceof' returns false if 'object' is null
        // so none of the clauses should be prone to errors
    }

    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.OCTET_STREAM, MediaType.valueOf("image/svg+xml") };
    }
}
