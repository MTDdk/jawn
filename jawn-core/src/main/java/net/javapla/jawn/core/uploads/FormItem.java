package net.javapla.jawn.core.uploads;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Charsets;

import net.javapla.jawn.core.Param;
import net.javapla.jawn.core.exceptions.ControllerException;
import net.javapla.jawn.core.util.StreamUtil;

/**
 * This interface represents a file or form item that was received within a
 * <code>multipart/form-data</code> POST request.
 * 
 * @author MTD
 *
 */
public interface FormItem {

    /**
     * File name.
     * @return file name.
     */
    public String getName();

    /**
     * Form field name.
     *
     * @return form field name
     */
    public String getFieldName();

    /**
     * Returns true if this is a file, false if not.
     *
     * @return true if this is a file, false if not.
     */
    public boolean isFile();
    

    /**
     * Return the size of the file
     * 
     * @return the size of the file
     * @author MTD
     */
    //public long getSize();
    
    /**
     * 
     * @return an indication of the file is usable
     * @author MTD
     */
    //public boolean isEmpty();
    
    /**
     * Content type of this form field.
     *
     * @return content type of this form field.
     */
    public String getContentType();
    //public void setContentType(String type);

    /**
     * returns true if this is a form field, false if not.
     *
     * @return true if this is a form field, false if not.
     */
    public boolean isFormField();

    /**
     * Returns input stream to read uploaded file contents from.
     *
     * @return input stream to read uploaded file contents from.
     */
    public InputStream openStream() throws IOException;

    /**
     * Converts entire content of this item to String.
     *
     * @return content streamed from this field as string.
     */
    public default String getStreamAsString() {
        try (InputStream is = openStream()) {
            return StreamUtil.read(is,Charsets.UTF_8);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
    
    /**
     * Converts the content of this item into a {@link Param}.<br>
     * Starts by converting into a string with {@link #getStreamAsString()}
     * 
     * @return
     *      The content as a representation as Param
     */
    public default Param asParam() {
        return new Param(getStreamAsString());
    }

    /**
     * Reads contents of a file into a byte array at once.
     *
     * @return contents of a file as byte array.
     */
    public default byte[] getBytes() {
        try (InputStream is = openStream()) {
            return StreamUtil.bytes(is);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }


    /**
     * Saves content of this item to a file.
     *
     * @param path to file
     * @throws IOException
     */
    public default void saveTo(String path) throws IOException {
        try (InputStream in = openStream()) {
            StreamUtil.saveTo(path, in);
        }
    }
}
