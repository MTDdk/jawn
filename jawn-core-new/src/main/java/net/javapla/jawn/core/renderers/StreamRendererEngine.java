package net.javapla.jawn.core.renderers;

import java.io.InputStream;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;

final class StreamRendererEngine implements RendererEngine {

    @Override
    public void invoke(final Context context, final Object obj) throws Exception {
        
        if (obj instanceof InputStream) {
            context.resp().send((InputStream) obj);
        } else if (obj instanceof byte[]) {
            context.resp().send((byte[]) obj);
        }
        
        // 'instanceof' returns false if 'object' is null
        // so none of the clauses should be prone to errors
    }

    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.OCTET_STREAM, MediaType.valueOf("image/svg+xml") };
    }

}
