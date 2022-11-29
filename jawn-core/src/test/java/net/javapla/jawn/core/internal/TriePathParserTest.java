package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.AssertionsHelper;
import net.javapla.jawn.core.internal.RouterImpl.TriePathParser;
import net.javapla.jawn.core.internal.RouterImpl.TriePathParser.TriePath;

class TriePathParserTest {

    @Test
    void simplePaths() {
        String p = "/simple";
        TriePath path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals(p, path.trieApplicable);
        assertFalse(path.hasParams);
        
        p = "/simple/more/segments";
        path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals(p, path.trieApplicable);
        assertFalse(path.hasParams);
    }
    
    @Test
    void segmentEnd() {
        String p = "/simple/{param}";
        TriePath path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals("/simple/*", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, "param");
    }
    
    @Test
    void segmentStart() {
        String p = "/{param}/simple";
        TriePath path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals("/*/simple", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, "param");
    }
    
    @Test
    void segmentMiddle() {
        String p = "/simple/{param}/route";
        TriePath path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals("/simple/*/route", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, "param");
    }

    @Test
    void multipleSegments() {
        String p = "/simple/{param1}/{param2}";
        TriePath path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals("/simple/*/*", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, "param1","param2");
        
        p = "/simple/{param1}/{param2}/route/more";
        path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals("/simple/*/*/route/more", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, "param1","param2");
        
        p = "/simple/{param1}/between/{param2}/route";
        path = TriePathParser.parse(p);
        assertEquals(p, path.original);
        assertEquals("/simple/*/between/*/route", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, "param1","param2");
    }
    
    @Test
    void errorneous() {
        //assertThrows(Up.class, () -> RoutePathParser.parse("/simple/{param/route"));
        
        TriePath path = TriePathParser.parse("/simple/{param/route");
        assertEquals("/simple/*/route", path.trieApplicable);
        AssertionsHelper.ass(path.parameterNames, "param");
        
        path = TriePathParser.parse("/simple/route/{param");
        assertEquals("/simple/route/*", path.trieApplicable);
        AssertionsHelper.ass(path.parameterNames, "param");
    }
}
