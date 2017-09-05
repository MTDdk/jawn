package net.javapla.jawn.core.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.ws.rs.core.MediaType;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

final class StreamTemplateEngine implements TemplateEngine {

    @Override
    public final void invoke(Context context, Result response, ResponseStream stream) {
        Object object = response.renderable();
        if (object instanceof InputStream) {
            try (   InputStream  in  = (InputStream) object;
                    OutputStream out = stream.getOutputStream()) {
                
                byte[] bytes = new byte[2048];
                
                int x;
                while((x = in.read(bytes)) != -1){
                    out.write(bytes, 0, x);
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (object instanceof Serializable) {
            try (ObjectOutputStream out = new ObjectOutputStream(stream.getOutputStream())) {
                out.writeObject(object);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (object instanceof byte[]) {
            byte[] arr = (byte[]) object;
            try (OutputStream out = stream.getOutputStream()) {
                out.write(arr);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 'instanceof' returns false if 'object' is null
        // so none of the clauses should be prone to errors
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        // intentionally null
        return null;
    }

    @Override
    public String[] getContentType() {
        return new String[]{MediaType.APPLICATION_OCTET_STREAM, "image/svg+xml"};
    }
    /*@Override
    public ContentType[] getContentType2() {
        return new ContentType[]{ContentType.APPLICATION_OCTET_STREAM};
    }*/

}
