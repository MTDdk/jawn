package net.javapla.jawn.core.internal.renderers;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;

@Singleton
class ImageRendererEngine extends StreamRendererEngine {
 // TODO ImageRendererEngine needs to be thoroughly thought through as well

    @Override
    public byte[] invoke(Context context, Object obj) throws Exception {


        if (obj instanceof BufferedImage) {

            // We assume the content type to be of the form "image/{extension}"
            // This is what we extract, as the image ought to be of the same format
            String extension = context.resp().contentType().subtype();//contentType.substring(contentType.indexOf('/')+1);

            // As we are using ImageIO as a serialiser, we need to ensure that it is actually going to 
            // do some work
            //if (!Arrays.asList(ImageIO.getWriterFileSuffixes()).contains(extension))
                throw new Up.BadMediaType(MessageFormat.format("An image writer could not be found for the extension {}", extension));

            /*ImageInputStream stream = ImageIO.createImageInputStream(obj);
            context.resp().send(stream);

            ImageIO.write((BufferedImage) obj, extension, context.resp());*/

        } /*else if (obj instanceof InputStream) {
            //super.invoke(context, obj);
            try (InputStream s = (InputStream) obj) {
                //context.resp().send(s);
                ((InputStream) obj).transferTo(context.resp().outputStream());
                //context.resp().outputStream().close();
            } catch (IOException e) {
                context.resp().outputStream().close();
            }
        } else if (obj instanceof File) {
            try (InputStream s = new FileInputStream((File)obj)) {
                context.resp().send(s);
            }
        }*/
        else //super.invoke(context, obj);
            return super.invoke(context, obj);

    }

    @Override
    public MediaType[] getContentType() {
        // Maps all the supported format names by ImageIO to MIME / content types
        return Arrays.asList(ImageIO.getWriterFormatNames()).stream().map((s) -> MediaType.valueOf("image/"+s)).toArray(MediaType[]::new);
    }

}
