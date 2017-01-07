package net.javapla.jawn.core.templates;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.imageio.ImageIO;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.http.Context;
import net.javapla.jawn.core.http.ResponseStream;

class ImageTemplateEngine implements TemplateEngine {

    @Override
    public void invoke(Context context, Result response, ResponseStream stream) {
        // We assume the content type to be of the form "image/{extension}"
        // This is what we extract, as the image ought to be of the same format
        String contentType = response.contentType();
        String extension = contentType.substring(contentType.indexOf('/')+1);
        
        // As we are using ImageIO as a serialiser, we need to ensure that it is actually going to 
        // do some work
        if (!Arrays.asList(ImageIO.getWriterFileSuffixes()).contains(extension))
            throw new MediaTypeException(MessageFormat.format("An image writer could not be found for the extension {}", extension));
        
        
        Object object = response.renderable();
        if (object instanceof BufferedImage) {
            try {
                ImageIO.write((BufferedImage) object, extension, stream.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        // Intentionally set to null
        return null;
    }

    @Override
    public String[] getContentType() {
        // Maps all the supported format names by ImageIO to MIME / content types
        return Arrays.asList(ImageIO.getWriterFormatNames()).stream().map((s) -> "image/"+s).toArray(String[]::new);
    }

}
