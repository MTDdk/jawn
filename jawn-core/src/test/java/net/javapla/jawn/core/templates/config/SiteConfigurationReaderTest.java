package net.javapla.jawn.core.templates.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.parsers.JsonMapperProvider;



public class SiteConfigurationReaderTest {
	
    static ObjectMapper objectMapper;
	static SiteConfigurationReader confReader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		objectMapper = new JsonMapperProvider().get();
		DeploymentInfo info = new DeploymentInfo(mock(JawnConfigurations.class), "");
		confReader = new SiteConfigurationReader(objectMapper, info);
	}

	@Test
	public void readingResources() {
		SiteConfiguration conf = confReader.read("src/test/resources", "index", "index", false);
		Assert.assertEquals("jawn test", conf.title);
		
		Assert.assertEquals(2, conf.scripts.length);
		Assert.assertEquals("/" + SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script1.js", conf.scripts[0].url);
		Assert.assertEquals("/" + SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script2.js", conf.scripts[1].url);
		
		Assert.assertEquals(2, conf.styles.length);
		Assert.assertEquals("/" + SiteConfigurationReader.STYLE_STANDARD_FOLDER + "style1.css", conf.styles[0].url);
		Assert.assertNull(conf.styles[0].attr.get("integrity"));
		Assert.assertNull(conf.styles[0].attr.get("crossorigin"));
		
		Assert.assertEquals("/" + SiteConfigurationReader.STYLE_STANDARD_FOLDER + "style2.css", conf.styles[1].url);
		Assert.assertEquals("#2", conf.styles[1].attr.get("integrity"));
		Assert.assertEquals("none", conf.styles[1].attr.get("crossorigin"));
		
		Assert.assertEquals(false, conf.overrideDefault);
	}
	
	@Test
    public void parsingScripts_should_addSlashJs() {
	    SiteConfiguration conf = confReader.read("src/test/resources", "index", "index", false);
        
	    Arrays.stream(conf.scripts).forEach(script -> Assert.assertTrue(script.url.contains("/js/")));
    }
    
    @Test
    public void parsingScripts_should_not_addAnything() {
        SiteConfiguration conf = confReader.read("src/test/resources", "nontranslatable", "index", false);
        Assert.assertEquals("jawn test non-translatable", conf.title);
        
        Arrays.stream(conf.scripts).forEach(script -> Assert.assertFalse(script.url.contains("/js/")));
    }
    
    //TODO
    // add test that catches or tells the user whenever there is an error when reading site.json
    // like in confReader.read("src/test/resources", "faulty", "index", false);
	
	@Test
	public void overrideDefault() {
		SiteConfiguration conf = confReader.read("src/test/resources", "override", "index", false);
		Assert.assertEquals("jawn test overridden", conf.title);
		Assert.assertEquals(true, conf.overrideDefault);
		
		Assert.assertEquals(1, conf.scripts.length);
		Assert.assertEquals("/" + SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script3.js", conf.scripts[0].url);
	}
	
	@Test
	public void asyncScripts() {
		SiteConfiguration conf = confReader.read("src/test/resources", "async", "index", false);
		Assert.assertEquals("jawn test async", conf.title);
	}
	
	@Test
    public void clone_should_includeAttributes() {
	    SiteConfiguration conf = confReader.read("src/test/resources", "index", "index", false);
	    
	    SiteConfiguration clone = conf.clone();
	    
	    Assert.assertEquals(conf.styles[1].attr.size(), clone.styles[1].attr.size());
	    conf.styles[1].attr.forEach((key,value) -> {
	        Assert.assertEquals(value, clone.styles[1].attr.get(key));
	    });
    }
	
	@Test
	public void merge() {
	    SiteConfiguration topConf = confReader.read("src/test/resources", "index", "index", false);
	    SiteConfiguration localConf = confReader.read("src/test/resources", "mergable", "index", false);
	    
	    Assert.assertNotEquals(topConf.title, localConf.title);
	    Assert.assertTrue(topConf.scripts.length < localConf.scripts.length);
	    Assert.assertTrue(topConf.styles.length == localConf.styles.length);
	}
	
	@Test
	public void mergeWithCache_should_not_affectOtherInstances() {
	    SiteConfiguration topConf = confReader.read("src/test/resources", "index", "index", true);
        SiteConfiguration localConf = confReader.read("src/test/resources", "mergable", "index", true);
        
        Assert.assertTrue(localConf.styles[0].attr.isEmpty());
        Assert.assertTrue(topConf.styles[0].attr.isEmpty());
        
        // we need to assert that a change to localConf does not affect the cached topConf
        localConf.styles[0].attr.put("some", "value");
        localConf.styles[0].attr.put("test", "value");
        
        Assert.assertFalse(localConf.styles[0].attr.isEmpty());
        Assert.assertTrue(topConf.styles[0].attr.isEmpty());
	}
	
	@Test
	public void merge_should_not_readTwiceWithLayoutInControllerFolder() {
        SiteConfiguration controllerConf = confReader.read("src/test/resources/controllerandlayoutequal", "controller", "controller/", false);
        
        assertEquals(3, controllerConf.scripts.length);
        assertEquals(3, controllerConf.styles.length);
	}
	
	@Test
    public void standardConf_should_not_readTwiceWithEmptyLayout() {
        SiteConfiguration conf = confReader.read("src/test/resources", "index", "", false);
        Assert.assertEquals("jawn test", conf.title);
        Assert.assertEquals(false, conf.overrideDefault);
        
        Assert.assertEquals(2, conf.scripts.length);
        Assert.assertEquals(2, conf.styles.length);
        
        conf = confReader.read("src/test/resources", "index", "/", false);
        Assert.assertEquals(2, conf.scripts.length);
        Assert.assertEquals(2, conf.styles.length);
    }
	
	@Test
	public void isLocal() {
	    Assert.assertTrue(SiteConfigurationReader.isLocal(""));
	    Assert.assertFalse(SiteConfigurationReader.isLocal("http://something.com"));
	    Assert.assertFalse(SiteConfigurationReader.isLocal("https://something.com"));
	    Assert.assertFalse(SiteConfigurationReader.isLocal("ftp://something.com"));
	    Assert.assertFalse(SiteConfigurationReader.isLocal("ftps://something.com"));
	    Assert.assertFalse(SiteConfigurationReader.isLocal("//something.com"));
	    Assert.assertTrue(SiteConfigurationReader.isLocal("file://something"));
	    Assert.assertTrue(SiteConfigurationReader.isLocal("something.css"));
	    Assert.assertTrue(SiteConfigurationReader.isLocal("something.js"));
	}
	
	@Test
	public void readSiteConfiguration_with_contextPath() {
	    JawnConfigurations configurations = mock(JawnConfigurations.class);
        DeploymentInfo info = new DeploymentInfo(configurations,"/certaincontext");
	    SiteConfigurationReader confReader = new SiteConfigurationReader(objectMapper, info);
        
	    SiteConfiguration conf = confReader.read("src/test/resources", "index", "index", false);
	    
        Assert.assertEquals("/certaincontext/"+SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script1.js", conf.scripts[0].url);
        Assert.assertEquals("/certaincontext/"+SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script2.js", conf.scripts[1].url);
        
        Assert.assertEquals("/certaincontext/"+SiteConfigurationReader.STYLE_STANDARD_FOLDER + "style1.css", conf.styles[0].url);
        Assert.assertEquals("/certaincontext/"+SiteConfigurationReader.STYLE_STANDARD_FOLDER + "style2.css", conf.styles[1].url);
	}
}
