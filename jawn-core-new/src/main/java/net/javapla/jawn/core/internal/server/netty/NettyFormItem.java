package net.javapla.jawn.core.internal.server.netty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import io.netty.handler.codec.http.multipart.FileUpload;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.util.MultiList;

final class NettyFormItem implements FormItem {
    
    private final String fieldName;
    
    private Optional<String> value = Optional.empty();
    
    private FileUpload data;
    private Optional<File> file = Optional.empty();
    
    NettyFormItem(final String fieldName, final FileUpload data, final Path tmpDir) throws IOException {
        this.fieldName = fieldName;
        
        this.data = data;
        
        //is this necessary?
        String name = "tmp-" + Long.toHexString(System.currentTimeMillis()) + "." + name();
        File f = tmpDir.resolve(name).toFile();
        data.renameTo(f);
        this.file = Optional.of(f);
    }
    
    NettyFormItem(final String fieldName, final String value) {
        this.fieldName = fieldName;
        this.value = Optional.of(value);
    }


    @Override
    public String name() {
        return fieldName;
    }

    @Override
    public Optional<String> value() {
        return value;
    }

    @Override
    public Optional<File> file() throws IOException {
        return file;
    }

    @Override
    public MultiList<String> headers() {
        MultiList<String> list = new MultiList<>();
        
        Optional.ofNullable(data.getContentTransferEncoding()).ifPresent( head ->
            list.put("Content-Transfer-Encoding", head)
        );
        
        Optional.ofNullable(data.getContentType()).ifPresent( head ->
            list.put("Content-Type", contentType())
        );
        
        list.put("Content-Disposition", "form-data; name=\""+fieldName+"\"" + Optional.ofNullable(data).map(data -> "; filename=\"" + data.getFilename() + "\"").orElse(""));
        
        return list;
    }

    @Override
    public String contentType() {
        String cs = Optional.ofNullable(data.getCharset())
            .map(it -> "; charset=" + it.name())
            .orElse("");
        return Optional.ofNullable(data.getContentType()).map(it -> it + cs).orElse("");
    }
    
    @Override
    public void close() throws IOException {
        file.ifPresent(File::delete);
        data.delete();
    }

}
