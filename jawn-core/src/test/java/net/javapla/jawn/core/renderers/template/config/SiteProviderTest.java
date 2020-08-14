package net.javapla.jawn.core.renderers.template.config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.util.Constants;

public class SiteProviderTest {
    
    private static final Path resources = Paths.get("src", "test", "resources", "renderers", "template", "config"); 
    
    static ObjectMapper objectMapper;
    static DeploymentInfo di;
    //static SiteProvider confReader;
    static Context context;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        objectMapper = new JsonMapperProvider().get();
        
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(resources.toString()));
        di = new DeploymentInfo(config, StandardCharsets.UTF_8, "");
        
        //confReader = new SiteProvider(objectMapper, di, Modes.PROD);
        
        Context.Request req = mock(Context.Request.class);
        when(req.path()).thenReturn("/test/mc/testen");
        
        context = mock(Context.class);
        when(context.req()).thenReturn(req);
    }

    @Test
    public void loadStandardSite() {
        
        Site site = new SiteProvider(objectMapper, di, Modes.PROD).load(context, Results.view(), "content"); // loads the standard site.json at the root of "resources"
        
        assertThat(site).isNotNull();
        assertThat(site.url).isEqualTo("/test/mc/testen");
        assertThat(site.title).isEqualTo("jawn test");
        assertThat(site.content).isEqualTo("content");
        assertThat(site.isProd()).isTrue();
        assertThat(site.isDev()).isFalse();
        assertThat(site.isTest()).isFalse();
        assertThat(site.scripts).isEqualTo("<script src=\"/js/script1.js\"></script><script src=\"/js/script2.js\"></script>");
        assertThat(site.styles).isEqualTo("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style1.css\"><link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style2.css\" integrity=\"#2\" crossorigin=\"none\">");
        
        assertThat(site.toString()).contains("mode[production]");
    }
    
    @Test
    public void modeDev() {
        Site site = new SiteProvider(objectMapper, di, Modes.DEV).load(context, Results.view(), "content");
        
        assertThat(site.isProd()).isFalse();
        assertThat(site.isDev()).isTrue();
        assertThat(site.isTest()).isFalse();
    }
    
}
