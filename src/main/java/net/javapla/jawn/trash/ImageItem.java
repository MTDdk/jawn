package net.javapla.jawn.trash;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import net.javapla.jawn.FormItem;

/**
 * 
 * @author MTD
 * @deprecated Not used anywhere
 */
public class ImageItem extends FormItem {
    
    /**
     * Field name and file name are set to File name.
     * If the file has a correct extension the content type is set to "image/&lt;extension&gt;".
     * Content type is set to "image/*", otherwise.
     *
     * @param file file to send.
     * @throws IOException
     */
    public ImageItem(File file) throws IOException {
        super(file.getName(), file.getName(), true, Files.probeContentType(file.toPath()), new FileInputStream(file));
    }
//    public ImageItem(FormItem item) {
////        super(item);
//        init();
//    }
//    public ImageItem(BufferedImage image, String filename) throws IOException {
//        super(filename, filename, true, "image/*", convert(image, filename));
//        init();
//    }
    private void init() {
//        String extension = FileName.extension(getName());
//        if (extension.length() > 0)
//            setConcentType("image/" + extension);
    }
    
//    static byte[] convert(BufferedImage image, String filename) throws IOException {
//        String extension = FileName.extension(filename);
//        if (extension.length() < 1) return null;
//        
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageIO.write(image, extension, os);
//        return os.toByteArray();
//    }
    
    /**
     * Calculates a unique filename located in the image upload folder.
     * 
     * WARNING: not threadsafe! Another file with the calculated name might very well get written onto disk first after this name is found 
     *  
     * @param filename
     * @return Outputs the relative path of the calculated filename.
     */
//    static String uniqueImagename(String folder, String filename) {
//        String buildFileName = filename.replace(' ', '_');
//        FileName fn = new FileName(buildFileName);
////        File output;//TODO use Path instead <- evidently, using Path would not pose a performance gain
//        
//        while ( (/*output = */new File(folder , buildFileName)).exists())
//            buildFileName = fn.increment();
//        
//        return buildFileName;//output.getPath();
//    }
    
    /**
     * @param folder Real path to save the image to.
     * @return The unique filename in the provided folder. Does not include the folder in the filename
     * @throws IOException
     */
//    public String saveToFolder(String folder) throws IOException {
//        String imagename = uniqueImagename(folder, this.getFileName());
//        this.saveTo(Paths.get(folder,imagename).toString());
//        return imagename;
//    }
    
}
