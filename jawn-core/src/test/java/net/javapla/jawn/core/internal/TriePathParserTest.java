package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.AssertionsHelper;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.internal.RouterImpl.TriePath;
import net.javapla.jawn.core.internal.RouterImpl.TriePathParser;

class TriePathParserTest {

    @Test
    void simplePaths() {
        String p = "/simple";
        TriePath path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals(p, path.trieApplicable);
        assertFalse(path.hasParams);
        
        p = "/simple/more/segments";
        path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals(p, path.trieApplicable);
        assertFalse(path.hasParams);
    }
    
    @Test
    void segmentEnd() {
        String p = "/simple/{param}";
        TriePath path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals("/simple/*", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, null, "param");
    }
    
    @Test
    void segmentStart() {
        String p = "/{param}/simple";
        TriePath path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals("/*/simple", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, "param", null);
    }
    
    @Test
    void segmentMiddle() {
        String p = "/simple/{param}/route";
        TriePath path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals("/simple/*/route", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, null, "param", null);
    }

    @Test
    void multipleSegments() {
        String p = "/simple/{param1}/{param2}";
        TriePath path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals("/simple/*/*", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, null, "param1","param2");
        
        p = "/simple/{param1}/{param2}/route/more";
        path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals("/simple/*/*/route/more", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, null, "param1","param2", null, null);
        
        p = "/simple/{param1}/between/{param2}/route";
        path = TriePathParser.parse(new Route.Builder(p).build());
        assertEquals("/simple/*/between/*/route", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.parameterNames, null, "param1", null, "param2", null);
    }
    
    @Test
    void erroneous() {
        //assertThrows(Up.class, () -> RoutePathParser.parse("/simple/{param/route"));
        
        TriePath path = TriePathParser.parse(new Route.Builder("/simple/{param/route").build());
        assertEquals("/simple/*/route", path.trieApplicable);
        AssertionsHelper.ass(path.parameterNames, null, "param", null);
        
        path = TriePathParser.parse(new Route.Builder("/simple/route/{param").build());
        assertEquals("/simple/route/*", path.trieApplicable);
        AssertionsHelper.ass(path.parameterNames, null, null, "param");
    }
}
