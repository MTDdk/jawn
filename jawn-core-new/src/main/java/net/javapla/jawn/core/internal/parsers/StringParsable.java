package net.javapla.jawn.core.internal.parsers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import net.javapla.jawn.core.parsers.ParserEngine;

public class StringParsable implements ParserEngine.Parsable {
    private final long length;
    private final Charset cs;
    private final String text;
    
    public StringParsable(final String text) {
        this(StandardCharsets.UTF_8, text);
    }
    
    public StringParsable(final Charset cs, final String text) {
        this.length = text ==  null ? 0 : text.length();
        this.cs = cs;
        this.text = text;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public byte[] bytes() throws IOException {
        return text.getBytes(cs);
    }

    /*@Override
    public String text() throws IOException {
        return text;
    }*/

}