package net.javapla.jawn.core.internal.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Handler;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;

public class AssetHandler implements Handler {

    private final static Logger logger = LoggerFactory.getLogger(AssetHandler.class.getSimpleName());
    private final static MediaType SVG = MediaType.byExtension("svg").get();
    
    private boolean etag = true;
    private boolean lastModified = false;
    private long maxAge = -1;
    
    private final DeploymentInfo deploymentInfo;
    
    public AssetHandler(final DeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }
    
    public AssetHandler etag(boolean activate) {
        this.etag = activate;
        return this;
    }
    
    public AssetHandler lastModified(boolean activate) {
        this.lastModified = activate;
        return this;
    }
    
    public AssetHandler maxAge(long seconds) {
        this.maxAge = seconds;
        return this;
    }
    
    @Override
    public Result handle(Context context) {
        final String path = context.req().path();
        
        final File file = new File(deploymentInfo.getRealPath(path));
        if (!file.canRead()) {
            return Results.notFound();
        }
        
        final Result result = Results.ok().contentType(MediaType.byPath(path).orElse(MediaType.OCTET_STREAM));
        
        
        if (_etag(result, file, context)) return result.status(Status.NOT_MODIFIED);
        if (_lastModified(result, file, context)) return result.status(Status.NOT_MODIFIED);
        _cacheControl(result);
        
        
        if (result.contentType().get().matches(SVG)) {
            result.header("mime-type","image/svg+xml");
            result.header("Content-Disposition", "");
        }
        
        try {
            return result.renderable(new FileInputStream(file)); // gets closed by the response (or at least it should be)
        } catch (IOException e) {
            logger.error("Something went wrong when rendering asset ", e);
            throw new Up.IO(e);
        }
    }
    
    /**
     * @param result
     * @param file
     * @param context
     * @return true, if not modified
     */
    private boolean _etag(final Result result, final File file, final Context context) {
        if (this.etag) {
            String etag = String.valueOf(file.lastModified());
            result.header("ETag", etag);
            
            return context.req()
                .header("If-None-Match")
                .map(etag::equals)
                .orElse(false);
        }
        return false;
    }
    
    /**
     * @param result
     * @param file
     * @param context
     * @return true, if not modified
     */
    private boolean _lastModified(final Result result, final File file, final Context context) {
        if (this.lastModified) {
            long lastModified = file.lastModified();
            result.header("Last-Modified", String.valueOf(lastModified));
            
            return context.req()
                .header("If-Modified-Since")
                .map(Long::parseLong)
                .map(modifiedSince -> lastModified <= modifiedSince)
                .orElse(false);
        }
        return false;
    }
    
    private void _cacheControl(final Result result) {
        if (maxAge > 0) {
            result.header("Cache-Control", "public, max-age=" + maxAge);
        }
    }

}