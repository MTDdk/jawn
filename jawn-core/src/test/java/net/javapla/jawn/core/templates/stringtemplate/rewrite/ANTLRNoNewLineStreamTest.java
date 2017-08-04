package net.javapla.jawn.core.templates.stringtemplate.rewrite;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;

import net.javapla.jawn.core.util.Constants;


public class ANTLRNoNewLineStreamTest {


    
    @Test
    public void template_should_removeNewLines_not_spaces() throws MalformedURLException, IOException {
        File file = new File("src/test/resources/webapp/views/antlrnonewlinestream.st");
        ANTLRNoNewLineStream templateStream = new ANTLRNoNewLineStream(file.toURI().toURL(), Constants.DEFAULT_ENCODING);
        
        String template = templateStream.substring(0, templateStream.size() - 1);
        System.out.println(template);
        
        // Currently, we do not care if a single space is preserved "wrongly" as we are agnostic to the type of template, which might not be HTML but something else
        String expected = "<p> Submit a hunting video, story, gear review, best practice article, or personal statement to share it with the hunting community.</p>";
        Assert.assertEquals(expected + expected, template);
    }
    
}
