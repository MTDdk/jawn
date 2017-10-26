package net.javapla.jawn.core.templates.config;

import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
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
		DeploymentInfo info = new DeploymentInfo(mock(JawnConfigurations.class));
		confReader = new SiteConfigurationReader(objectMapper, info);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void readingResources() {
		SiteConfiguration conf = confReader.read("src/test/resources", "index", "index", false);
		Assert.assertEquals("jawn test", conf.title);
		
		Assert.assertEquals(2, conf.scripts.length);
		Assert.assertEquals(SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script1.js", conf.scripts[0].url);
		Assert.assertEquals(SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script2.js", conf.scripts[1].url);
		
		Assert.assertEquals(2, conf.styles.length);
		Assert.assertEquals(SiteConfigurationReader.STYLE_STANDARD_FOLDER + "style1.css", conf.styles[0].url);
		Assert.assertNull(conf.styles[0].integrity);
		Assert.assertNull(conf.styles[0].crossorigin);
		Assert.assertEquals(SiteConfigurationReader.STYLE_STANDARD_FOLDER + "style2.css", conf.styles[1].url);
		Assert.assertEquals("#2", conf.styles[1].integrity);
		Assert.assertEquals("none", conf.styles[1].crossorigin);
		
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
		Assert.assertEquals(SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script3.js", conf.scripts[0].url);
	}
	
	@Test
	public void asyncScripts() {
		SiteConfiguration conf = confReader.read("src/test/resources", "async", "index", false);
		Assert.assertEquals("jawn test async", conf.title);
	}
	
	/*@Test
	public void readSiteConfiguration_with_contextPath() {
	    JawnConfigurations configurations = mock(JawnConfigurations.class);
	    when(configurations.getSecure(Constants.PROPERTY_DEPLOYMENT_INFO_CONTEXT_PATH)).thenReturn(Optional.of("/certaincontext"));
        DeploymentInfo info = new DeploymentInfo(configurations);
	    SiteConfigurationReader confReader = new SiteConfigurationReader(objectMapper, info);
        
	    SiteConfiguration conf = confReader.read("src/test/resources", "index", "index", false);
	    
        Assert.assertEquals("/certaincontext"+SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script1.js", conf.scripts[0].url);
        Assert.assertEquals("/certaincontext"+SiteConfigurationReader.SCRIPT_STANDARD_FOLDER + "script2.js", conf.scripts[1].url);
        
        Assert.assertEquals("/certaincontext"+SiteConfigurationReader.STYLE_STANDARD_FOLDER + "style.css", conf.styles[0]);
	}*/
}
