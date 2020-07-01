package net.javapla.jawn.core.internal.renderers;

import java.nio.ByteBuffer;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;

@Singleton
final class TextRendererEngine extends StreamRendererEngine /*implements RendererEngine*/ {

    @Override
    public final void invoke(final Context context, final Object obj) throws Exception {
        if (obj instanceof ByteBuffer) {
            context.resp().send( (ByteBuffer) obj);
        } else if (obj instanceof String) {
            context.resp().send( (String) obj );
        } /*else if (obj instanceof byte[]) {
            context.resp().send( (byte[]) obj);
        } else if (obj instanceof InputStream) {
            new StreamRendererEngine().invoke(context, obj);
        } */else {
            //context.resp().send( obj.toString() );
            super.invoke(context, obj);
        }
    }

    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.PLAIN, MediaType.TEXT, MediaType.valueOf("text/javascript"), MediaType.valueOf("text/css") };
    }
}
