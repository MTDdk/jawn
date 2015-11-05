package net.javapla.jawn.core.images;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.javapla.jawn.core.FormItem;
import net.javapla.jawn.core.Response;
import net.javapla.jawn.core.ResponseBuilder;
import net.javapla.jawn.core.ResponseHolder;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.exceptions.MediaTypeException;
import net.javapla.jawn.core.http.Context;

import org.imgscalr.Scalr;

/**
 * 
 * @author MTD
 */
public class ImageHandlerBuilder {
    private final ResponseHolder holder;
    private final Context context;
    
    private BufferedImage image;
    private final FileName fn = new FileName();

    private ImageHandlerBuilder(ResponseHolder holder, Context context) {
        this.holder = holder;
        this.context = context;
    }
    
    public ImageHandlerBuilder(ResponseHolder holder, Context context, FormItem item) throws ControllerException {
        this(holder, context);
        try {
            this.image = ImageIO.read(item.getInputStream());
            fn.updateNameAndExtension(item.getFileName());
            
            if (this.image == null)
                throw new ControllerException("The extension '" + fn.extension() + "' could not be read");
        } catch (IOException e) {
            throw new ControllerException(e);
        }
    }
    public ImageHandlerBuilder(ResponseHolder holder, Context context, File file) throws ControllerException {
        this(holder, context);
        try {
            this.image = ImageIO.read(file);
            fn.updateNameAndExtension(file.getName());
            
            if (this.image == null)
                throw new ControllerException("The extension '" + fn.extension() + "' could not be read");
        } catch (IOException e) {
            throw new ControllerException(e);
        }
    }
    public ImageHandlerBuilder(ResponseHolder holder, Context context, byte[] bytes, String fileName) throws ControllerException {
        this(holder, context);
        try {
            this.image = ImageIO.read(new ByteArrayInputStream(bytes));
            fn.updateNameAndExtension(fileName);
            
            if (this.image == null)
                throw new ControllerException("The extension '" + fn.extension() + "' could not be read");
        } catch (IOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * @param filename Include extension of the image
     */
     public ImageHandlerBuilder name(String filename) {
         fn.updateNameAndExtension(filename);
         return this;
     }

     public ImageHandlerBuilder append(String appendage) {
         fn.appendToFilename(appendage);
         return this;
     }

     /**
      * Crop the image to the given width and height, using (x,y) as coordinates for upper left corner
      * @param x
      * @param y
      * @param width
      * @param height
      * @return
      */
     public ImageHandlerBuilder crop(int x, int y, int width, int height) {
         height = Math.min(height, image.getHeight() - y); 
         width = Math.min(width, image.getWidth() - x);

         BufferedImage crop = Scalr.crop(image, x, y, width, height);
         image.flush();// cannot throw
         image = crop;
         return this;
     }
     
     /**
      * Crop the image to a given aspect.
      * <br>
      * The aspect is the desired proportions of the image, which will be kept when cropping.
      * <p>
      * The usecase for this method is when you have an image, that you want to crop into a certain proportion,
      * but want to keep the original dimensions as much as possible.
      * @param width
      * @param height
      * @return
      */
     public ImageHandlerBuilder cropToAspect(int width, int height) {
         int original_width = image.getWidth();
         int original_height = image.getHeight();
         int bound_width = width;
         int bound_height = height;
         int new_width = original_width;
         int new_height = original_height;
         int x = 0, y = 0;
         
         // We need these if any resizing is going to happen, as the precision factors
         // are no longer sufficiently precise
         double wfactor = (double)original_width/bound_width;
         double hfactor = (double)original_height/bound_height;
         
         // Calculating the factor between the original dimensions and the wanted dimensions
         // Using precision down to two decimals.
         double precision = 100d;
         double wprecisionfactor = ((int)((wfactor) * precision)) / precision;
         double hprecisionfactor = ((int)((hfactor) * precision)) / precision;
         
         
         if (wprecisionfactor == hprecisionfactor) {
             // they are (relatively) equal
             // just use the original image dimensions
             return this;
         }
         
         if (wprecisionfactor < hprecisionfactor) {
             // keep the original width
             // calculate the new height from the wfactor
             new_height = (int) (bound_height * wfactor);
             
             // calculate new coordinate to keep center
             y = Math.abs(original_height - new_height) >> 1; // divide by 2
         } else if (wprecisionfactor > hprecisionfactor) {
             // keep the original height
             // calculate the new width from the hfactor
             new_width = (int) (bound_width * hfactor);
             
             // calculate new coordinate to keep center
             x = Math.abs(original_width - new_width) >> 1; // divide by 2
         }
         
         BufferedImage crop = Scalr.crop(image, x, y, new_width, new_height);
         image.flush();// cannot throw
         image = crop;
         
         return this;
     }

     /**
      * Resize the image to the given width and height
      * @param width
      * @param height
      * @return
      */
     public ImageHandlerBuilder resize(int width, int height) {
         BufferedImage resize = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);
         image.flush();
         image = resize;
         return this;
     }
     /**
      * Resize the image to a given height, maintaining the original proportions of the image
      * and setting the width accordingly.
      * If the height of the image is smaller than the provided size, then the image is not scaled.
      * @param size
      * @return
      */
     public ImageHandlerBuilder resizeToHeight(int size) {
         if (image.getHeight() < size) return this;
         BufferedImage resize = Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, Math.min(size, image.getHeight()));
         image.flush();
         image = resize;
         return this;
     }
     /**
      * Resize the image to a given width, maintaining the original proportions of the image
      * and setting the height accordingly.
      * If the width of the image is smaller than the provided size, then the image is not scaled.
      * @param size
      * @return
      */
     public ImageHandlerBuilder resizeToWidth(int size) {
         if (image.getWidth() < size) return this;
         BufferedImage resize = Scalr.resize(image, Scalr.Mode.FIT_TO_WIDTH, size);
         image.flush();
         image = resize;
         return this;
     }

     /**
      * <p>Sends the image to the outputstream of the response.</p>
      * 
      * If the file has a correct extension the content type is set to "image/&lt;extension&gt;",
      * otherwise an exception is thrown.
      * 
      * @throws ControllerException If something go wrong during write to the outputstream
      * @throws MediaTypeException If a suitable writer for the image extension could not be found.
      */
     public void send() throws ControllerException, MediaTypeException {
         String extension = fn.extension();

         int status = 200;
         String contentType = "image/"+extension;
         Response response = ResponseBuilder
                 .ok()
                 .contentType(contentType)
                 .status(status)
                 .addSupportedContentType("image/*")
                 .renderable(this.image);
         holder.setControllerResponse(response);
     }

     /**
      * Saves the file on the server in the folder given
      * 
      * @param uploadFolder
      * @return The server path to the saved file.
      * @throws ControllerException If anything goes wrong during write to disk.
      */
     public String save(String uploadFolder) throws ControllerException {
         String realPath = context.getRealPath(uploadFolder);

         fn.sanitise();

         String imagename = uniqueImagename(realPath, fn.fullPath());
         try {
             ImageIO.write(image, fn.extension(), new File(realPath, imagename));
             image.flush();
         } catch (IOException e) {
             throw new ControllerException(e);
         }
         return uploadFolder + File.separatorChar + imagename;
     }
     
     public byte[] asBytes() {
         try {
             ByteArrayOutputStream stream = new ByteArrayOutputStream();
             ImageIO.write(image, fn.extension(), stream);
             byte[] array = stream.toByteArray();
             return array;
         } catch (IOException e) {
             throw new ControllerException(e);
         }
     }
     
     public ImageHandlerBuilder clone() {
         ImageHandlerBuilder builder = new ImageHandlerBuilder(holder, context);
         builder.fn.updateName(this.fn.filename());
         builder.fn.updateExtension(this.fn.extension());
         builder.image = deepCopy(this.image);
         return builder;
     }
     
     public static final BufferedImage deepCopy(BufferedImage bi) {
         ColorModel cm = bi.getColorModel();
         boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
         WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
         return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
     }

     /**
      * Calculates a unique filename located in the image upload folder.
      * 
      * WARNING: not threadsafe! Another file with the calculated name might very well get written onto disk first after this name is found 
      *  
      * @param filename
      * @return Outputs the relative path of the calculated filename.
      */
     String uniqueImagename(String folder, String filename) {
         String buildFileName = filename;//.replace(' ', '_');

         while ( (new File(folder , buildFileName)).exists())
             buildFileName = fn.increment();

         return buildFileName;//output.getPath();
     }
}