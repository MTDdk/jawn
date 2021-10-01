package net.javapla.jawn.core.internal.renderers;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.renderers.RendererEngine;

@Singleton
final class XmlRendererEngine implements RendererEngine {

    private final XmlMapper mapper;
    
    @Inject
    public XmlRendererEngine(final XmlMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public byte[] invoke(final Context context, final Object obj) throws Exception {
//        if (obj instanceof byte[]) {
//            context.resp().send((byte[])obj);
//        } else if (obj instanceof String) {
//            context.resp().send( /*(String)obj );/*/((String) obj).getBytes(context.resp().charset()));
//        } else {
//            context.resp().send(mapper.writeValueAsBytes(obj));
//        }
        return null;
    }

    @Override
    public MediaType[] getContentType() {
        return new MediaType[]{ MediaType.XML };
    }
}
