package net.javapla.jawn;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


class ApacheFileItemFacade implements FileItemStream {
    private String name, fieldName, contentType;
    private boolean isFile;
    private InputStream inputStream;
    private long size;



    ApacheFileItemFacade(String name, String fieldName, String contentType, boolean isFile, byte[] content) {
        this.name = name;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFile = isFile;
        this.inputStream = new ByteArrayInputStream(content);
        this.size = content.length;
    }
    
    ApacheFileItemFacade(String name, String fieldName, String contentType, boolean isFile, InputStream stream) {
        this.name = name;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFile = isFile;
        this.inputStream = stream;
        
        if (stream == null) { this.size = 0; }
        else {
            try {
                this.size = stream.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ApacheFileItemFacade(org.apache.commons.fileupload.FileItem apacheFileItem) throws IOException {
        this.name = apacheFileItem.getName();
        this.fieldName = apacheFileItem.getFieldName();
        this.contentType = apacheFileItem.getContentType();
        this.isFile = !apacheFileItem.isFormField();
        this.inputStream = apacheFileItem.getInputStream();
        this.size = apacheFileItem.getSize();
    }

    @Override
    public InputStream openStream() throws IOException {
        if(inputStream != null){
            return inputStream;
        }else{
            throw new RuntimeException("this should never happen :(");
        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String type) {
        contentType = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean isFormField() {
        return !isFile;
    }

    @Override
    public FileItemHeaders getHeaders() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setHeaders(FileItemHeaders fileItemHeaders) {
        throw new UnsupportedOperationException("not implemented");
    }
    
    /**
     * Get the file size
     * 
     * @author ALVN
     * @return
     */
    public long getSize() {
        return size;
    }
}
