package net.javapla.jawn.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import net.javapla.jawn.core.AssertionsHelper;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.internal.RouterImpl.TriePath;
import net.javapla.jawn.core.internal.RouterImpl.TriePathParser;

class TriePathParserTest {

    @Test
    void simplePaths() {
        String p = "/simple";
        TriePath path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass(p, path.trieApplicable);
        assertFalse(path.hasParams);
        
        p = "/simple/more/segments";
        path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass(p, path.trieApplicable);
        assertFalse(path.hasParams);
    }
    
    @Test
    void segmentEnd() {
        String p = "/simple/{param}";
        TriePath path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass("/simple/#", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.segments, null, "param");
    }
    
    @Test
    void wildcardEnd() {
        String p = "/simple/more/{*param}";
        TriePath path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass("/simple/more/*", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(SEGMENT_MAPPER, path.segments, null, null, "param");
    }
    
    @Test
    void segmentStart() {
        String p = "/{param}/simple";
        TriePath path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass("/#/simple", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.segments, "param", null);
    }
    
    @Test
    void segmentMiddle() {
        String p = "/simple/{param}/route";
        TriePath path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass("/simple/#/route", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.segments, null, "param", null);
    }

    @Test
    void multipleSegments() {
        String p = "/simple/{param1}/{param2}";
        TriePath path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass("/simple/#/#", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.segments, null, "param1","param2");
        
        p = "/simple/{param1}/{param2}/route/more";
        path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass("/simple/#/#/route/more", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.segments, null, "param1","param2", null, null);
        
        p = "/simple/{param1}/between/{param2}/route";
        path = TriePathParser.parse(route(p).build());
        AssertionsHelper.ass("/simple/#/between/#/route", path.trieApplicable);
        assertTrue(path.hasParams);
        AssertionsHelper.ass(path.segments, null, "param1", null, "param2", null);
    }
    
    @Test
    void parseRequest() {
        String p = "/simple/{param}";
        TriePath path = TriePathParser.parse(route(p).build());
        TriePath parsed = TriePathParser.parseRequest("/simple/pathparam", path);
        assertTrue(parsed.hasParams);
        assertTrue(parsed.isStatic);
        assertEquals("pathparam", parsed.pathParameters.get("param"));
    }
    
    @Test
    void parseRequest_with_wildcard() {
        String p = "/simple/more/{*filepath}";
        TriePath path = TriePathParser.parse(route(p).build());
        TriePath parsed = TriePathParser.parseRequest("/simple/more/folder/image.jpg", path);
        assertTrue(parsed.hasParams);
        assertTrue(parsed.isStatic);
        assertEquals("folder/image.jpg", parsed.pathParameters.get("filepath"));
    }
    
    @Test
    void erroneous() {
        //assertThrows(Up.class, () -> RoutePathParser.parse("/simple/{param/route"));
        
        TriePath path = TriePathParser.parse(route("/simple/{param/route").build());
        AssertionsHelper.ass("/simple/#/route", path.trieApplicable);
        AssertionsHelper.ass(path.segments, null, "param", null);
        
        path = TriePathParser.parse(route("/simple/route/{param").build());
        AssertionsHelper.ass("/simple/route/#", path.trieApplicable);
        AssertionsHelper.ass(path.segments, null, null, "param");
    }
    
    private Route.Builder route(String p) {
        return new Route.Builder(HttpMethod.GET, p, ctx -> ctx);
    }
    static final Function<RouterImpl.Segment, String> SEGMENT_MAPPER = segment -> segment == null ? null : segment.name();
}
