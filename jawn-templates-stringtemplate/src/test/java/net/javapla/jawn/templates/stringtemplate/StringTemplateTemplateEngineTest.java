package net.javapla.jawn.templates.stringtemplate;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.AdditionalAnswers;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Context.Response;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.View;
import net.javapla.jawn.core.renderers.template.ViewTemplateLoader;
import net.javapla.jawn.core.renderers.template.config.Site;
import net.javapla.jawn.core.renderers.template.config.SiteProvider;
import net.javapla.jawn.core.renderers.template.config.TemplateConfigProvider;
import net.javapla.jawn.core.util.AsyncCharArrayWriter;
import net.javapla.jawn.core.util.Constants;

public class StringTemplateTemplateEngineTest {
    
    private static final Path resources = Paths.get("src", "test", "resources", "webapp");
    private static StringTemplateTemplateEngine engine; 
    
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(resources.toString()));
        DeploymentInfo di = new DeploymentInfo(config, StandardCharsets.UTF_8, "");
        ViewTemplateLoader templateLoader = new ViewTemplateLoader(di, Modes.PROD);

        
        // Mock the SiteProvider to return a Site with actual content in it
        SiteProvider siteProvider = mock(SiteProvider.class);
        when(siteProvider.load(any(Context.class), any(View.class), any()))
            .then(AdditionalAnswers.answer((Context ctx, View view, Object content) -> Site.builder(Modes.PROD).build().content(content)));
        
        
        engine = new StringTemplateTemplateEngine(new TemplateConfigProvider<StringTemplateConfiguration>(), Modes.PROD, templateLoader, siteProvider);
    }

    @Test
    public void standardTemplating() throws Exception {
        Response response = mock(Response.class);
        AsyncCharArrayWriter writer = new AsyncCharArrayWriter(61);
        when(response.writer()).thenReturn(writer);
        
        Context context = mock(Context.class);
        when(context.resp()).thenReturn(response);
        
        
        // execute
        engine.invoke(context, Results.view());
        
        
        // verify
        /*doAnswer(AdditionalAnswers.answerVoid((CharBuffer buffer) -> {
            assertThat(buffer.toString()).isEqualTo("<html><body><div>epic content</div></body></html>");
        })).when(response).send(any(CharBuffer.class));
        verify(response, times(1)).send(any(CharBuffer.class));*/
        verify(response, times(1)).writer();
        assertThat(writer.toString()).isEqualTo("<html><body><div>epic content</div></body></html>");
    }
    
    @Test
    public void noTagLeftover() throws Exception {
        Response response = mock(Response.class);
        AsyncCharArrayWriter writer = new AsyncCharArrayWriter(61);
        when(response.writer()).thenReturn(writer);
        
        Context context = mock(Context.class);
        when(context.resp()).thenReturn(response);

        
        // execute
        engine.invoke(context, Results.view().layout("additional"));
        
        
        // verify
        /*doAnswer(AdditionalAnswers.answerVoid((CharBuffer buffer) -> {
            assertThat(buffer.toString()).isEqualTo("<html><body>additional<div>epic content</div></body></html>");
        })).when(response).send(any(CharBuffer.class));
        verify(response, times(1)).send(any(CharBuffer.class));*/
        verify(response, times(1)).writer();
        assertThat(writer.toString()).isEqualTo("<html><body>additional<div>epic content</div></body></html>");
    }
    
    @Test
    public void inject() throws Exception {
        Response response = mock(Response.class);
        AsyncCharArrayWriter writer = new AsyncCharArrayWriter(61);
        when(response.writer()).thenReturn(writer);
        
        Context context = mock(Context.class);
        when(context.resp()).thenReturn(response);

        
        // execute
        engine.invoke(context, Results.view().template("injectcontent").put("test", "injected content"));

        
        // verify
        /*doAnswer(AdditionalAnswers.answerVoid((CharBuffer buffer) -> {
            assertThat(buffer.toString()).isEqualTo("<html><body><div>injected content</div></body></html>");
        })).when(response).send(any(CharBuffer.class));
        verify(response, times(1)).send(any(CharBuffer.class));*/
        verify(response, times(1)).writer();
        assertThat(writer.toString()).isEqualTo("<html><body><div>injected content</div></body></html>");
    }

    @Test
    public void simpleInvoke() {
        
        // execute
        String output = engine.invoke(Results.view().template("injectcontent").layout("index").put("test", "Mail:<br>simple template"));
        
        // assert
        assertThat(output).isEqualTo("<html><body><div>Mail:<br>simple template</div></body></html>");
    }
}
