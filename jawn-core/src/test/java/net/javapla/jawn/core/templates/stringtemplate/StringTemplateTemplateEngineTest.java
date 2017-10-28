package net.javapla.jawn.core.templates.stringtemplate;

import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stringtemplate.v4.misc.ErrorBuffer;

import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.templates.config.SiteConfiguration;
import net.javapla.jawn.core.templates.config.SiteConfigurationReader;
import net.javapla.jawn.core.templates.config.TemplateConfigProvider;
import net.javapla.jawn.core.util.Modes;

public class StringTemplateTemplateEngineTest {

	static StringTemplateTemplateEngine engine;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		JawnConfigurations conf = new JawnConfigurations(Modes.DEV);
		engine = new StringTemplateTemplateEngine(new TemplateConfigProvider<StringTemplateConfiguration>(), conf, new DeploymentInfo(conf) , new SiteConfigurationReader(new JsonMapperProvider().get(),new DeploymentInfo(mock(JawnConfigurations.class))));
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
	public void parsingScriptsFormatting() {
		
		SiteConfiguration.Script[] scripts = new SiteConfiguration.Script[]{
			new SiteConfiguration.Script("script1.js",false, false),
			new SiteConfiguration.Script("script2.js",true, true),
			new SiteConfiguration.Script("script3.js", "test_type", "test_integrity", "test_crossorigin", false, false)
		};
		
		String html = engine.createLinks(scripts);
		
		Assert.assertTrue(html.indexOf("script1.js") < html.indexOf("script2.js"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("defer"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("async"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("defer"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("script3.js"));
		Assert.assertTrue(html.indexOf("script3.js") < html.indexOf("integrity=\"test_integrity\""));
		Assert.assertTrue(html.indexOf("script3.js") < html.indexOf("crossorigin=\"test_crossorigin\""));

	}
	
	@Test
	public void parsingStylesFormatting() {
		SiteConfiguration.Style[] styles = new SiteConfiguration.Style[] {
				new SiteConfiguration.Style("style1.css"),
				new SiteConfiguration.Style("style2.css", "test_integrity", "test_crossorigin")
		};
		
		String html = engine.createLinks(styles);
		
		Assert.assertTrue(html.indexOf("style1.css") < html.indexOf("style2.css"));
		Assert.assertTrue(html.indexOf("style2.css") < html.indexOf("integrity=\"test_integrity\""));
		Assert.assertTrue(html.indexOf("style2.css") < html.indexOf("crossorigin=\"test_crossorigin\""));
		
	}

}
