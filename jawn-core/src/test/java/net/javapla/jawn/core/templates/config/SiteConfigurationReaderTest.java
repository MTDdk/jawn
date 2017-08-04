package net.javapla.jawn.core.templates.config;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.javapla.jawn.core.parsers.JsonMapperProvider;



public class SiteConfigurationReaderTest {
	
	static SiteConfigurationReader confReader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ObjectMapper objectMapper = new JsonMapperProvider().get();
		confReader = new SiteConfigurationReader(objectMapper);
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
		Assert.assertEquals("script1.js", conf.scripts[0].url);
		Assert.assertEquals("script2.js", conf.scripts[1].url);
		
		Assert.assertEquals(1, conf.styles.length);
		Assert.assertEquals("style.css", conf.styles[0]);
		
		Assert.assertEquals(false, conf.overrideDefault);
	}
	
	@Test
	public void overrideDefault() {
		SiteConfiguration conf = confReader.read("src/test/resources", "override", "index", false);
		Assert.assertEquals("jawn test overridden", conf.title);
		Assert.assertEquals(true, conf.overrideDefault);
		
		Assert.assertEquals(1, conf.scripts.length);
		Assert.assertEquals("script3.js", conf.scripts[0].url);
	}
	
	@Test
	public void asyncScripts() {
		SiteConfiguration conf = confReader.read("src/test/resources", "async", "index", false);
		Assert.assertEquals("jawn test async", conf.title);
	}
	
}
