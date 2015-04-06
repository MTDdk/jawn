package net.javapla.jawn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.imageio.ImageIO;

import net.javapla.jawn.exceptions.ControllerException;
import net.javapla.jawn.exceptions.MediaTypeException;

import org.imgscalr.Scalr;

/**
 * Always assumes, that the image is to be saved in the configured imageUploadFolder
 * @author MTD
 */
public class ImageHandlerBuilder {
    private final Context context;
    
    //TODO actually this is up to the user to figure out. There should not be a default folder
    private final String uploadPath = "uploads/images";//Configuration.imageUploadFolder();
    private BufferedImage image;
    private final FileName fn = new FileName();

    public ImageHandlerBuilder(Context context, FormItem item) throws ControllerException {
        this.context = context;
        try {
            this.image = ImageIO.read(item.getInputStream());
            fn.updateNameAndExtension(item.getFileName());
        } catch (IOException e) {
            throw new ControllerException(e);
        }
    }
    public ImageHandlerBuilder(Context context, File file) throws ControllerException {
        this.context = context;
        try {
            this.image = ImageIO.read(file);
            String fullpath = file.getPath();
            folder(fullpath);
            name(fullpath);
        } catch (IOException e) {
            throw new ControllerException(e);
        }
        
        //  M-dd-yy
        // [h:mm:ss]
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
      * Assumes, that the <code>folder</code> is relative to "uploads/images"
      * @param folder
      * @return
      */
     public ImageHandlerBuilder folder(String folder) {
         if (folder.contains(uploadPath)) 
             folder = folder.substring( folder.indexOf(uploadPath) + uploadPath.length() +1 );
         fn.updatePath(folder);
         return this;
     }

     public ImageHandlerBuilder crop(int x, int y, int width, int height) {
         height = Math.min(height, image.getHeight() - y); 
         width = Math.min(width, image.getWidth() - x);

         BufferedImage crop = Scalr.crop(image, x, y, width, height);
         image.flush();// cannot throw
         image = crop;
         return this;
     }

     public ImageHandlerBuilder resize(int width, int height) {
         BufferedImage resize = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);
         image.flush();
         image = resize;
         return this;
     }
     public ImageHandlerBuilder resizeToHeight(int size) {
         BufferedImage resize = Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, size);
         image.flush();
         image = resize;
         return this;
     }
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
         if (!Arrays.asList(ImageIO.getWriterFileSuffixes()).contains(extension))
             throw new MediaTypeException(MessageFormat.format("An image writer could not be found for the extension {}", extension));

         try {
             int status = 200;
             String contentType = "image/"+extension;
             context.setNewControllerResponse(NewControllerResponseBuilder.noContent().contentType(contentType).status(status));
//             context.setControllerResponse(new NopResponse(context, contentType, status));
             ImageIO.write(this.image, extension, context.responseOutputStream());//outputStream(contentType, null, status));
         } catch (IOException e) {
             throw new ControllerException(e);
         }
     }

     /**
      * Saves the file on the server in the folder stated in {@link Configuration#imageUploadFolder()}
      * (default is uploads/images)
      * 
      * @return The server path to the saved file.
      * @throws ControllerException If anything goes wrong during write to disk.
      */
     public String save() throws ControllerException {
         String realPath = context.getRealPath(uploadPath);

         // sanitise
         fn.apply((s) -> s.replace(' ', '_'));

         String imagename = uniqueImagename(realPath, fn.fullPath());
         try {
             ImageIO.write(image, fn.extension(), new File(realPath, imagename));
             image.flush();
         } catch (IOException e) {
             throw new ControllerException(e);
         }
         return uploadPath + File.separatorChar + imagename;
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
         //            Filename fn = new Filename(buildFileName);

         while ( (new File(folder , buildFileName)).exists())
             buildFileName = fn.increment();

         return buildFileName;//output.getPath();
     }
}