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
import java.util.stream.Collectors;

public interface Parsable extends Iterable<Parsable> {
    
    long length();
    byte[] bytes() throws IOException;
    //String text() throws IOException;
    
    @Override
    default Iterator<Parsable> iterator() {
        return Collections.emptyIterator();
    }
    
    
    static Parsable of(final byte[] bytes) {
        return new BytesParsable(bytes);
    }
    static Parsable of(final long length, final File file) {
        return new FileParsable(length, file);
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
    
    
    static class BytesParsable implements Parsable {
        
        private final long length;
        
        private File file;
        private byte[] bytes;
        
        public BytesParsable(final byte[] bytes) {
            this.length = bytes.length;
            this.bytes = bytes;
        }
        
        @Override
        public long length() {
            return length;
        }

        @Override
        public byte[] bytes() throws IOException {
            if (bytes == null)
                return Files.readAllBytes(file.toPath());
            return bytes;
        }

        /*@Override
        public String text() throws IOException {
            if (bytes != null) return new String(bytes, cs);
            return new String(bytes(), cs);
        }*/
    }
    
    static class FileParsable implements Parsable {
        
        private final long length;
        private final File file;
        
        public FileParsable(final long length, final File file) {
            this.length = length;
            this.file = file;
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public byte[] bytes() throws IOException {
            return Files.readAllBytes(file.toPath());
        }

    }
    
    static class StringParsable implements Parsable {
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
    }
    
    static class ListParsable implements Parsable {
        
        private final List<Parsable> list;
        
        public ListParsable(List<Parsable> parsables) {
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

    }
}