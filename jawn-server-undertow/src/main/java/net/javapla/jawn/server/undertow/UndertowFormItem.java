package net.javapla.jawn.server.undertow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.util.HttpString;
import net.javapla.jawn.core.uploads.FormItem;


public class UndertowFormItem implements FormItem {
    
    private final FormValue value;
    private final String fieldName;

    public UndertowFormItem(FormValue value, String fieldName) {
        this.value = value;
        this.fieldName = fieldName;
    }

    @Override
    public String getName() {
        if  (isFile()) return value.getFileName();
        return value.getValue();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean isFile() {
        return value.isFile();
    }

    @Override
    public String getContentType() {
        return value.getHeaders().getFirst(HttpString.tryFromString("Content-Type"));
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(value.getPath().toFile());
    }


}
