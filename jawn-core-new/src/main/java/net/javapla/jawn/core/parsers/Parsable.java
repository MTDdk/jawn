package net.javapla.jawn.core.parsers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface Parsable extends Iterable<Parsable> {
    
    long length();
    byte[] bytes() throws IOException;
    String text() throws IOException;
    
    @Override
    default Iterator<Parsable> iterator() {
        return Collections.emptyIterator();
    }
    
    
    static Parsable of(final byte[] bytes, final Charset cs) {
        return new BytesParsable(bytes, cs);
    }
    static Parsable of(final long length, final File file, final Charset cs) {
        return new FileParsable(length, file, cs);
    }
    static Parsable of(final String text) {
        return new StringParsable(text);
    }
    static Parsable of(final Charset cs, final String text) {
        return new StringParsable(cs, text);
    }
    static Parsable of(final String ... parsables) {
        return new ListParsable(Arrays.asList(parsables).stream().map(StringParsable::new).collect(Collectors.toList()));
    }
    static Parsable of(final Charset cs, final String ... parsables) {
        return new ListParsable(Arrays.asList(parsables).stream().map(s -> of(cs, s)).collect(Collectors.toList()));
    }
    static Parsable of(final Parsable ... parsables) {
        return new ListParsable(Arrays.asList(parsables));
    }
    static Parsable of(final List<Parsable> parsables) {
        return new ListParsable(parsables);
    }
    
    static Parsable of(final Optional<String> opt) {
        return opt.map(StringParsable::new).orElse(new StringParsable());
    }
    
    static class BytesParsable implements Parsable {
        
        private final long length;
        private final byte[] bytes;
        private final Charset cs;

        
        BytesParsable(final byte[] bytes, final Charset cs) {
            this.length = bytes.length;
            this.bytes = bytes;
            this.cs = cs;
        }
        
        @Override
        public long length() {
            return length;
        }

        @Override
        public byte[] bytes() throws IOException {
            return bytes;
        }

        @Override
        public String text() throws IOException {
            return new String(bytes, cs);
        }
    }
    
    static class FileParsable implements Parsable {
        
        private final long length;
        private final File file;
        private final Charset cs;
        
        FileParsable(final long length, final File file, final Charset cs) {
            this.length = length;
            this.file = file;
            this.cs = cs;
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public byte[] bytes() throws IOException {
            return Files.readAllBytes(file.toPath());
        }
        
        @Override
        public String text() throws IOException {
            return new String(bytes(), cs);
        }

    }
    
    static class StringParsable implements Parsable {
        private final long length;
        private final Charset cs;
        private final String text;
        
        StringParsable() {
            this(null);
        }
        
        StringParsable(final String text) {
            this(StandardCharsets.UTF_8, text);
        }
        
        StringParsable(final Charset cs, final String text) {
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
        
        @Override
        public String text() throws IOException {
            return text;
        }
    }
    
    static class ListParsable implements Parsable {
        
        private final List<Parsable> list;
        
        ListParsable(List<Parsable> parsables) {
            this.list = parsables;
        }

        @Override
        public Iterator<Parsable> iterator() {
            return list.iterator();
        }

        @Override
        public long length() {
            return list.size();
        }

        @Override
        public byte[] bytes() throws IOException {
            return list.get(0).bytes();
        }

        @Override
        public String text() throws IOException {
            return list.get(0).text();
        }

    }
}