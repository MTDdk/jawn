package net.javapla.jawn.core.templates.config;

import org.junit.Assert;
import org.junit.Test;

import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.Modes;

public class SiteBuilderTest {
    
    @Test
    public void simpleScriptsFormatting() {
        SiteConfiguration.Tag[] scripts = new SiteConfiguration.Tag[]{
            new SiteConfiguration.Tag("script1.js")
        };
        
        String html = Site.builder(Modes.DEV).createScripts(scripts);
        
        Assert.assertEquals("<script src=\"script1.js\"></script>\n", html);
    }
    
    @Test
    public void simpleScriptsFormattingNotDEV() {
        SiteConfiguration.Tag[] scripts = new SiteConfiguration.Tag[]{
            new SiteConfiguration.Tag("script1.js")
        };
        
        String html = Site.builder(Modes.TEST).createScripts(scripts);
        Assert.assertEquals("<script src=\"script1.js\"></script>", html);
        
        html = Site.builder(Modes.PROD).createScripts(scripts);
        Assert.assertEquals("<script src=\"script1.js\"></script>", html);
    }
    
    @Test
	public void parsingScriptsFormatting() {
		
		SiteConfiguration.Tag[] scripts = new SiteConfiguration.Tag[]{
			new SiteConfiguration.Tag("script1.js"),
			new SiteConfiguration.Tag("script2.js",
			    CollectionUtil.map(
                    "defer","true",
                    "async","true"
                )
			),
			new SiteConfiguration.Tag("script3.js", 
			    CollectionUtil.map(
                    "type","test_type",
                    "integrity","test_integrity",
                    "crossorigin","test_crossorigin",
                    "defer","false",
                    "async","false"
                )
			)
		};
		
		String html = Site.builder(Modes.DEV).createScripts(scripts);
		
		Assert.assertTrue(html.indexOf("script1.js") < html.indexOf("script2.js"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("defer"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("async"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("defer"));
		Assert.assertTrue(html.indexOf("script2.js") < html.indexOf("script3.js"));
		Assert.assertTrue(html.indexOf("script3.js") < html.indexOf("integrity=\"test_integrity\""));
		Assert.assertTrue(html.indexOf("script3.js") < html.indexOf("crossorigin=\"test_crossorigin\""));

	}
	
	@Test
    public void simpleStylesFormatting() {
	    SiteConfiguration.Tag[] styles = new SiteConfiguration.Tag[] {
            new SiteConfiguration.Tag("style1.css")
	    };
        
        String html = Site.builder(Modes.DEV).createStyles(styles);
        
        Assert.assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"style1.css\">\n", html);
    }
	@Test
    public void advancedTagsFormatting() {
        SiteConfiguration.Tag[] styles = new SiteConfiguration.Tag[] {
            new SiteConfiguration.Tag("style1.css"),
            new SiteConfiguration.Tag("style2.css", 
                CollectionUtil.map(
                    "defer","true",
                    "async","true",
                    "integrity","integritySHA"
                )
            )
        };
        
        String html = Site.builder(Modes.DEV).createStyles(styles);
        
        // attributes are sorted due to the nature of HashMap
        Assert.assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"style1.css\">\n"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"style2.css\" async=\"true\" defer=\"true\" integrity=\"integritySHA\">\n", html);
    }

    @Test
	public void parsingStylesFormatting() {
		SiteConfiguration.Tag[] styles = new SiteConfiguration.Tag[] {
				new SiteConfiguration.Tag("style1.css"),
				new SiteConfiguration.Tag("style2.css", 
				    CollectionUtil.map(
	                    "integrity","test_integrity",
	                    "crossorigin","test_crossorigin"
	                )
				)
		};
		
		String html = Site.builder(Modes.DEV).createStyles(styles);
		
		Assert.assertTrue(html.indexOf("style1.css") < html.indexOf("style2.css"));
		Assert.assertTrue(html.indexOf("style2.css") < html.indexOf("integrity=\"test_integrity\""));
		Assert.assertTrue(html.indexOf("style2.css") < html.indexOf("crossorigin=\"test_crossorigin\""));
		
	}

}
