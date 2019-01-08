package net.javapla.jawn.core.internal.server.undertow;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import net.javapla.jawn.core.server.FormItem;
import net.javapla.jawn.core.util.MultiList;

final class UndertowFormItem implements FormItem {
    
    private final FormValue value;
    private final String fieldName;
    
    UndertowFormItem(final FormValue value, final String fieldName) {
        this.value = value;
        this.fieldName = fieldName;
    }

    @Override
    public String name() {
        return fieldName;
    }

    @Override
    public Optional<String> value() {
        return !value.isFileItem() ? Optional.of(value.getValue()) : Optional.empty();
    }

    @Override
    public Optional<File> file() throws IOException {
        return value.isFileItem() ? Optional.of(value.getFileItem().getFile().toFile()) : Optional.empty();
    }

    @Override
    public MultiList<String> headers() {
        MultiList<String> h = new MultiList<>();
        
        HeaderMap values = value.getHeaders();
        if (values == null) return h;
        
        for (var it = values.iterator(); it.hasNext();) {
            HeaderValues header = it.next();
            header.forEach(v -> h.put(header.getHeaderName().toString(), v));
        }
        
        return h;
    }

    @Override
    public String contentType() {
        return value.getHeaders().getFirst(HttpString.tryFromString("Content-Type"));
    }

}
