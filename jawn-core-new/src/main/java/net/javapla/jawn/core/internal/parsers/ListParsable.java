package net.javapla.jawn.core.internal.parsers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import net.javapla.jawn.core.parsers.ParserEngine;

public class ListParsable implements ParserEngine.Parsable {
    
    private final List<ParserEngine.Parsable> list;
    
    public ListParsable(String ... parsables) {
        this.list = Arrays.asList(parsables).stream().map(StringParsable::new).collect(Collectors.toList());
    }
    
    public ListParsable(ParserEngine.Parsable ... parsables) {
        this.list = Arrays.asList(parsables);
    }
    
    public ListParsable(List<ParserEngine.Parsable> parsables) {
        this.list = parsables;
    }

    @Override
    public Iterator<ParserEngine.Parsable> iterator() {
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