package net.javapla.jawn.core.renderers;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.renderers.template.ViewTemplateLoader;
import net.javapla.jawn.core.renderers.template.ViewTemplates;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;

public class ViewTemplateLoaderTest {
    
    static final String templateEnding = ".template";
    
    static ViewTemplateLoader templateLoader;
    static DeploymentInfo di;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(Paths.get("src", "test", "resources", "webapp").toString()));
        
        di = new DeploymentInfo(config, StandardCharsets.UTF_8, "");
        
        templateLoader = new ViewTemplateLoader(di, Modes.DEV);
    }
    
    @Test
    public void standardView() throws FileNotFoundException, IOException {
        ViewTemplates template = templateLoader.load(Results.view(), templateEnding/*, false*/);
        
        assertThat(template.templatePath()).isEqualTo("/index" + templateEnding);
        assertThat(template.layoutPath()).isEqualTo("/index.html" + templateEnding);
        
        String layout = template.layout();//StreamUtil.read(template.layoutAsReader());
        assertThat(layout).isEqualTo(
            "<html>\n" + 
            "<head></head>\n" + 
            "<body></body>\n" + 
            "</html>");
    }
    
    @Test//(expected = FileNotFoundException.class)
    public void standardView_noTemplateOnlyLayout() throws FileNotFoundException, IOException {
        ViewTemplates template = templateLoader.load(Results.view(), templateEnding/*, false*/);
        
        assertThat(template.templateFound()).isFalse();
        //template.templateAsReader();
    }
    
    @Test
    public void templateNoLF() {
        ViewTemplateLoader loader = new ViewTemplateLoader(di, Modes.PROD); // <-- PROD = remove all \n + \r + trim
        ViewTemplates templates = loader.load(Results.view(), templateEnding);
        
        String layout = templates.layout();
        assertThat(layout).isEqualTo(
            "<html>" + 
            "<head></head>" + 
            "<body></body>" + 
            "</html>"); // no line-feed
    }
    
    /*@Test
    public void complexView() {
        
    }*/
    
    @Test
    public void useStandardLayout() {
        ViewTemplates template = templateLoader.load(Results.view().path("nolayout"), templateEnding/*, false*/);
        
        assertThat(template.templatePath()).isEqualTo("/nolayout/index" + templateEnding);
        assertThat(template.layoutPath()).isEqualTo("/index.html" + templateEnding);
        
        assertThat(template.templateFound()).isTrue();
    }
    
    @Test
    public void readStandardTemplate() throws FileNotFoundException, IOException {
        ViewTemplates template = templateLoader.load(Results.view().path("nolayout"), templateEnding/*, false*/);
        
        String read = template.template();//StreamUtil.read(template.templateAsReader());
        assertThat(read).isEqualTo(
            "<div class=\"content\">use standard layout</div>");
    }
    
    @Test
    public void overrideLayout() {
        ViewTemplates template = templateLoader.load(Results.view().path("withlayout"), templateEnding/*, false*/);
        
        assertThat(template.templatePath()).isEqualTo("/withlayout/index" + templateEnding);
        assertThat(template.layoutPath()).isEqualTo("/withlayout/index.html" + templateEnding);
        
        assertThat(template.templateFound()).isTrue();
    }
    
    @Test
    public void readTemplateFromLongerPath() throws FileNotFoundException, IOException {
        ViewTemplates template = templateLoader.load(Results.view().path("withlayout"), templateEnding/*, false*/);
        
        String read = template.template();//StreamUtil.read(template.templateAsReader());
        assertThat(read).isEqualTo(
            "<div class=\"content\">use adjacent layout</div>");
    }
    
    @Test
    public void nonDefaultTemplate() {
        ViewTemplates template = templateLoader.load(Results.view().path("nolayout").template("update"), templateEnding/*, false*/);
        
        assertThat(template.templatePath()).isEqualTo("/nolayout/update" + templateEnding);
        assertThat(template.layoutPath()).isEqualTo("/index.html" + templateEnding);
        
        assertThat(template.templateFound()).isTrue();
    }

    @Test(expected = Up.ViewError.class) // The ending is wrong, and a layout or template can therefore not be found, which
    public void layoutMissing() {
        templateLoader.load(Results.view(), ".kein"/*, false*/);
    }
    
    public void noLayout() {
        ViewTemplates template = templateLoader.load(Results.view().path("nolayout").template("update").layout(null), templateEnding/*, false*/);
        
        assertThat(template.templatePath()).isEqualTo("nolayout/update" + templateEnding);
        assertThat(template.layoutPath()).isEqualTo(null);
        assertThat(template.layoutFound()).isFalse();
    }
}
