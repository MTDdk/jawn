package net.javapla.jawn.templates.stringtemplate.rewrite;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.junit.Test;
import org.mockito.AdditionalAnswers;


public class ANTLRNoNewLineStreamTest {

    @Test
    public void template_should_removeNewLines_not_spaces() throws MalformedURLException, IOException {
        File file = new File("src/test/resources/webapp/views/antlrnonewlinestream.st");
        ANTLRNoNewLineStream templateStream = new ANTLRNoNewLineStream(file.toURI().toURL(), "UTF-8");
        
        String template = templateStream.substring(0, templateStream.size() - 1);
        
        // Currently, we do not care if a single space is preserved "wrongly" as we are agnostic to the type of template, which might not be HTML but something else
        // (like the space directly after <p>)
        String expected = "<p> Submit a hunting video, story, gear review, best practice article, or personal statement to share it with the hunting community.</p>";
        assertThat(template).isEqualTo(expected + expected);
    }
    
    @Test
    public void makeSureResourceIsClosed() throws MalformedURLException, IOException {
        
        InputStream input =  mock(InputStream.class);
        when(input.read(any(byte[].class), anyInt(), anyInt()))
            // first do this
            .thenAnswer(AdditionalAnswers.answer((byte[] b) -> {
                b[0] = 'A';
                b[1] = 'Z';
                return 2;
            }))
            // then do this by the next call
            .thenReturn(-1);

        
        
        ANTLRNoNewLineStream templateStream = new ANTLRNoNewLineStream(input, "UTF-8");

        
        
        verify(input, atLeastOnce()).close();
        assertThat(templateStream.size()).isEqualTo(2);
        assertThat(templateStream.substring(0, templateStream.size() - 1)).isEqualTo("AZ");
    }
}
