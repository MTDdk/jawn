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
	public void parsingScripts_should_writeAsync() {
		
		SiteConfiguration.Script[] scripts = new SiteConfiguration.Script[]{
			new SiteConfiguration.Script("script1.js",false),
			new SiteConfiguration.Script("script2.js",true)
		};
		
		ErrorBuffer error = new ErrorBuffer();
		String html = engine.readLinks(StringTemplateTemplateEngine.SCRIPTS_TEMPLATE, scripts, error);
		
		Assert.assertTrue(error.errors.isEmpty());
		Assert.assertTrue(html.contains("async defer>"));
	}

}
