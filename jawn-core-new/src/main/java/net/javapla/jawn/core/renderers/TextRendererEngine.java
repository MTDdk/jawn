package net.javapla.jawn.core.renderers;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;

@Singleton
final class TextRendererEngine implements RendererEngine {

    @Override
    public final void invoke(final Context context, final Object obj) throws Exception {
        if (obj instanceof String) {
            context.resp().send(((String) obj).getBytes(context.req().charset()));
        } else if (obj instanceof byte[]) {
            context.resp().send((byte[]) obj);
        } else {
            context.resp().send(obj.toString().getBytes(context.req().charset()));
        }
    }

    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.PLAIN, MediaType.TEXT };
    }

}
