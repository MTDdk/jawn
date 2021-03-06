package net.javapla.jawn.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.imgscalr.Scalr;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Up.IO;
import net.javapla.jawn.core.server.FormItem;

public class Images {
    
    public static enum ImageFormat {
        JPG("jpg","jpeg"),GIF("gif"),BMP("bmp"),TIF("tif","tiff"),PNG("png"),WBMP("wbmp");
        
        final String[] names;
        private ImageFormat(String ... names) {
            this.names = names;
        }
        
        String[] names() {
            return names;
        }
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
        
        public static ImageFormat fromExtension(final String ext) throws IllegalArgumentException {
            for (ImageFormat format : values()) {
                for (var n : format.names) {
                    if (n.equals(ext)) {
                        return format;
                    }
                }
            }
            throw new IllegalArgumentException("None found with ext [" + ext + "]");
        }
        
        public static ImageFormat fromFileName(final String ext) throws IllegalArgumentException {
            int dot = ext.lastIndexOf('.');
            if (dot < 1) throw new IllegalArgumentException(ext);
            return fromExtension(ext.substring(dot + 1).toLowerCase());
        }
        
        public static ImageFormat from(final File file) throws IllegalArgumentException {
            return fromFileName(file.getName());
        }
        
        /**
         * @param contentType Like "image/gif", "image/jpg", "image/png"
         * @return
         */
        public static ImageFormat fromContentType(final String contentType) throws IllegalArgumentException {
            return fromExtension(contentType.substring(contentType.lastIndexOf('/') + 1));
        }
    }
    
    public static enum ImageQuality {
        FAST,BALANCED,QUALITY,EXTREME_QUALITY;
        
        Scalr.Method toScalr() {
            switch (this) {
                case EXTREME_QUALITY: return Scalr.Method.ULTRA_QUALITY;
                case QUALITY: return Scalr.Method.QUALITY;
                case FAST: return Scalr.Method.SPEED;
                case BALANCED:
                default: return Scalr.Method.BALANCED;
            }
            //return Scalr.Method.AUTOMATIC;
        }
    }
    
    public interface Image {

        /**
         * Crop the image to the given width and height, using (x,y) as coordinates for upper left corner
         * @param x
         * @param y
         * @param width
         * @param height
         * @return this
         */
        Image crop(int x, int y, int width, int height);

        /**
         * Crop the image to a given aspect.
         * <br>
         * The aspect is the desired proportions of the image, which will be kept when cropping.
         * <p>
         * The usecase for this method is when you have an image, that you want to crop into a certain proportion,
         * but want to keep the original dimensions as much as possible.
         * @param width
         * @param height
         * @return this
         */
        Image cropToAspect(int width, int height);
        
        /**
         * Resize the image to the given width and height
         * @param width
         * @param height
         * @return this
         */
        Image resize(int width, int height);
        Image resize(int width, int height, ImageQuality method);
        
        /**
         * Resize the image to a given height, maintaining the original proportions of the image
         * and setting the width accordingly.
         * If the height of the image is smaller than the provided size, then the image is not scaled.
         * @param size
         * @return this
         */
        Image resizeToHeight(int size);
        Image resizeToHeight(int size, ImageQuality method);
        
        /**
         * Resize the image to a given width, maintaining the original proportions of the image
         * and setting the height accordingly.
         * If the width of the image is smaller than the provided size, then the image is not scaled.
         * @param size
         * @return this
         */
        Image resizeToWidth(int size);
        Image resizeToWidth(int size, ImageQuality method);
        
        
        /**
         * Uses compression quality of <code>qualityPercent</code>.
         * Can be applied multiple times
         * @param qualityPercent the percentage of compression quality wanted. Must be between 0.0 and 1.0
         * @return this for chaining
         */
        Image reduceQuality(float qualityPercent);
        
        Image rotate90clockwise();
        Image rotate180clockwise();
        
        byte[] asBytes();
        
        BufferedImage asBufferedImage();
        
        
        /**
         * Creates a {@link Result} that can be used to send this image
         * 
         * If the file has a correct extension the content type is set to "image/&lt;extension&gt;",
         * otherwise an exception is thrown.
         */
        Result asResult();
        
        Image save(Path path) throws Up.IO;
        Image save(Path path, OpenOption ... options) throws Up.IO;
        
        void flush();
    }
    
    public static Image image(final FormItem item) throws Up.IO {
        try {
            return image(ImageIO.read(Files.newInputStream(item.file().orElseThrow())), ImageFormat.fromFileName(item.fileName().get()));
        } catch (IOException e) {
            throw new Up.IO(e); // this is not really an IO error..
        }
    }
    
    public Image image(final File file) throws Up.IO {
        try {
            return image(ImageIO.read(file), ImageFormat.from(file));
        } catch (IllegalArgumentException | IOException e) {
            throw new Up.IO(e); // this is not really an IO error..
        }
    }
    
    public static Image image(final InputStream stream, final ImageFormat format) throws Up.IO {
        try {
            return image(ImageIO.read(stream), format);
        } catch (IOException e) {
            throw new Up.IO(e); // this is not really an IO error..
        }
    }
    
    public static Image image(final byte[] bytes, final ImageFormat format) throws Up.IO {
        try {
            return image(ImageIO.read(new ByteArrayInputStream(bytes)), format);
        } catch (IOException e) {
            throw new Up.IO(e); // this is not really an IO error..
        }
    }
    
    public static Image image(final Path path) throws Up.IO {
        try {
            String file = path.getFileName().toString();
            String name = file.substring(0, file.lastIndexOf('.'));
            
            return image(ImageIO.read(Files.newInputStream(path, StandardOpenOption.READ)), ImageFormat.fromFileName(name));
        } catch (IllegalArgumentException | IOException e) {
            throw new Up.IO(e); // this is not really an IO error..
        }
    }
    
    public static Image image(final BufferedImage img, final ImageFormat format) {
        
        return new Image() {
            private BufferedImage image = img;

            @Override
            public Image crop(int x, int y, int width, int height) {
                int h = Math.min(height, image.getHeight() - y); 
                int w = Math.min(width, image.getWidth() - x);

                BufferedImage crop = Scalr.crop(image, x, y, w, h);
                image.flush();// cannot throw
                image = crop;
                return this;
            }

            @Override
            public Image cropToAspect(int width, int height) {
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

            @Override
            public Image resize(int width, int height) {
                return resize(width, height, ImageQuality.BALANCED);
            }
            
            @Override
            public Image resize(int width, int height, ImageQuality method) {
                BufferedImage resize = Scalr.resize(image, method.toScalr(), Scalr.Mode.FIT_EXACT, width, height);
                image.flush();
                image = resize;
                return this;
            }

            @Override
            public Image resizeToHeight(int size) {
                return resizeToHeight(size, ImageQuality.BALANCED);
            }
            
            @Override
            public Image resizeToHeight(int size, ImageQuality method) {
                if (image.getHeight() < size) return this;
                BufferedImage resize = Scalr.resize(image, method.toScalr(), Scalr.Mode.FIT_TO_HEIGHT, Math.min(size, image.getHeight()));
                image.flush();
                image = resize;
                return this;
            }

            @Override
            public Image resizeToWidth(int size) {
                return resizeToWidth(size, ImageQuality.BALANCED);
            }
            
            @Override
            public Image resizeToWidth(int size, ImageQuality method) {
                if (image.getWidth() < size) return this;
                BufferedImage resize = Scalr.resize(image, method.toScalr(), Scalr.Mode.FIT_TO_WIDTH, size);
                image.flush();
                image = resize;
                return this;
            }

            @Override
            public Image reduceQuality(float qualityPercent) {
                ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(format.toString()).next();
                if (imageWriter != null) {
                    ImageWriteParam writeParam = imageWriter.getDefaultWriteParam();
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionQuality(qualityPercent);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageWriter.setOutput(new MemoryCacheImageOutputStream(baos));
                    try {
                        imageWriter.write(null, new IIOImage(image, null, null), writeParam);
                        imageWriter.dispose();
                    } catch (IOException e) {
                        throw new Up.IO(e); // this is not really an IO error..
                    }
                    
                    try {
                       BufferedImage lowImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                       image.flush();
                       image = lowImage;
                   } catch (IOException e) {
                       throw new Up.IO(e); // this is not really an IO error..
                   }
                }
                return this;
            }
            
            @Override
            public Image rotate90clockwise() {
                BufferedImage resize = Scalr.rotate(img, Scalr.Rotation.CW_90);
                image.flush();
                image = resize;
                return this;
            }
            
            @Override
            public Image rotate180clockwise() {
                BufferedImage resize = Scalr.rotate(img, Scalr.Rotation.CW_180);
                image.flush();
                image = resize;
                return this;
            }

            @Override
            public byte[] asBytes() {
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ImageIO.write(image, format.toString(), stream);
                    return stream.toByteArray();
                } catch (IOException e) {
                    throw new Up.IO(e); // this is not really an IO error..
                }
            }
            
            @Override
            public BufferedImage asBufferedImage() {
                return image;
            }
            
            @Override
            public Image save(Path path) throws Up.IO {
                // does not overwrite
                return save(path, StandardOpenOption.CREATE);
            }
            
            @Override
            public Image save(Path path, OpenOption... options) throws IO {
                try {
                    ImageIO.write(image, format.name().toLowerCase(), Files.newOutputStream(path, options));
                    return this;
                } catch (IOException e) {
                    throw new Up.IO(e);
                }
            }
            
            @Override
            public void flush() {
                image.flush();
            }
            
            @Override
            public Result asResult() {
                String contentType = "image/"+format.toString();
                return Results.ok(img).contentType(contentType);
            }
        };
    }
    
}
