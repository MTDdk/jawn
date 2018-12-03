package net.javapla.jawn.core.server;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * File upload from multipart/form-data post.
 */
public interface UploadItem {
    
    String name();
    
    List<String> headers(String name);

    File file() throws IOException;
}
