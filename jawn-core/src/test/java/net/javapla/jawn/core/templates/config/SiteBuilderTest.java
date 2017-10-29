package net.javapla.jawn.core.templates.config;

import org.junit.Assert;
import org.junit.Test;

public class SiteBuilderTest {
    
    @Test
    public void simpleScriptsFormatting() {
        SiteConfiguration.Script[] scripts = new SiteConfiguration.Script[]{
            new SiteConfiguration.Script("script1.js",false, false)
        };
        
        String html = Site.builder().createLinks(scripts);
        
        Assert.assertEquals("<script src=\"script1.js\"></script>", html);
    }
    
    @Test
	public void parsingScriptsFormatting() {
		
		SiteConfiguration.Script[] scripts = new SiteConfiguration.Script[]{
			new SiteConfiguration.Script("script1.js",false, false),
			new SiteConfiguration.Script("script2.js",true, true),
			new SiteConfiguration.Script("script3.js", "test_type", "test_integrity", "test_crossorigin", false, false)
		};
		
		String html = Site.builder().createLinks(scripts);
		
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
	    SiteConfiguration.Style[] styles = new SiteConfiguration.Style[] {
            new SiteConfiguration.Style("style1.css")
    };
        
        String html = Site.builder().createLinks(styles);
        
        Assert.assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"style1.css\">", html);
    }

    @Test
	public void parsingStylesFormatting() {
		SiteConfiguration.Style[] styles = new SiteConfiguration.Style[] {
				new SiteConfiguration.Style("style1.css"),
				new SiteConfiguration.Style("style2.css", "test_integrity", "test_crossorigin")
		};
		
		String html = Site.builder().createLinks(styles);
		
		Assert.assertTrue(html.indexOf("style1.css") < html.indexOf("style2.css"));
		Assert.assertTrue(html.indexOf("style2.css") < html.indexOf("integrity=\"test_integrity\""));
		Assert.assertTrue(html.indexOf("style2.css") < html.indexOf("crossorigin=\"test_crossorigin\""));
		
	}

}
