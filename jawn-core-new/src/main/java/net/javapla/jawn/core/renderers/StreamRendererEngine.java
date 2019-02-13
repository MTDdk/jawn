package net.javapla.jawn.core.renderers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;

@Singleton
class StreamRendererEngine implements RendererEngine {

    @Override
    public void invoke(final Context context, final Object obj) throws Exception {
        
        if (obj instanceof InputStream) {
            context.resp().send((InputStream) obj);
        } else if (obj instanceof byte[]) {
            context.resp().send((byte[]) obj);
        } else if (obj instanceof File) {
            try (FileInputStream stream = new FileInputStream((File) obj)) {
                context.resp().send(stream);
            }
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
