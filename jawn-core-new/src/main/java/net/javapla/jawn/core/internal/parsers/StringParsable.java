package net.javapla.jawn.core.internal.parsers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import net.javapla.jawn.core.parsers.ParserEngine;

public class StringParsable implements ParserEngine.Parsable {
    
    private final String s;
    private final Charset cs;

    public StringParsable(final String s) {
        this(s, StandardCharsets.UTF_8);
    }
    
    public StringParsable(final String s, final Charset cs) {
        this.s = s;
        this.cs = cs;
    }

    @Override
    public byte[] bytes() throws IOException {
        return text().getBytes(cs);
    }

    @Override
    public String text() throws IOException {
        return s;
    }

    @Override
    public long length() {
        return s == null ? 0 : s.length();
    }

}
