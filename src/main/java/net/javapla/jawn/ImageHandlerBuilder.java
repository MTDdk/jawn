package net.javapla.jawn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.MediaTypeException;

import org.imgscalr.Scalr;

/**
 * 
 * @author MTD
 */
public class ImageHandlerBuilder {
    private final ControllerResponseHolder holder;
    private final Context context;
    
    private BufferedImage image;
    private final FileName fn = new FileName();

    private ImageHandlerBuilder(ControllerResponseHolder holder, Context context) {
        this.holder = holder;
        this.context = context;
    }
    
    public ImageHandlerBuilder(ControllerResponseHolder holder, Context context, FormItem item) throws ControllerException {
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
    public ImageHandlerBuilder(ControllerResponseHolder holder, Context context, File file) throws ControllerException {
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
      * @param size
      * @return
      */
     public ImageHandlerBuilder resizeToHeight(int size) {
         BufferedImage resize = Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, size);
         image.flush();
         image = resize;
         return this;
     }
     /**
      * Resize the image to a given width, maintaining the original proportions of the image
      * and setting the height accordingly.
      * @param size
      * @return
      */
     public ImageHandlerBuilder resizeToWidth(int size) {
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
         ControllerResponse response = ControllerResponseBuilder
                 .ok()
                 .contentType(contentType)
                 .status(status)
                 .addSupportedContentType("image/*")
                 .renderable(this.image);
         holder.setControllerResponse(response);
     }

     /**
      * Saves the file on the server in the folder stated in {@link ConfigApp#imageUploadFolder()}
      * (default is uploads/images)
      * 
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