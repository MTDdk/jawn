package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Assets;
import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Context.Request;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.util.Constants;

public class AssetHandlerTest {
    
    private static String resources = Paths.get("src", "test", "resources", "webapp").toString();
    private static DeploymentInfo di;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(resources));
        di = new DeploymentInfo(config, StandardCharsets.UTF_8, "");
    }

    @Test
    public void handle() {
        Route.Handler css = new AssetHandler(di);
        
        Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.path()).thenReturn("/css/dummy.css");
        
        Result result = css.handle(context);
        assertThat(result.status().get()).isEqualTo(Status.OK);
        assertThat(result.charset().isPresent()).isFalse(); // not a part of the request
        assertThat(result.contentType().get().matches(MediaType.TEXT)).isTrue();
        assertThat(result.renderable().isPresent()).isTrue();
    }
    
    @Test
    public void handle_without_care() {
        Route.Handler css = new AssetHandler(di);
        
        Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.path()).thenReturn("/css/notfound.css");
        
        Result result = css.handle(context);
        assertThat(result.status().get()).isEqualTo(Status.NOT_FOUND);
        assertThat(result.contentType().get().matches(MediaType.PLAIN)).isTrue();
        assertThat(result.renderable().isPresent()).isFalse();
    }
    
    @Test
    public void handle_svg() {
        Route.Handler img = new AssetHandler(di);
        
        Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.path()).thenReturn("/img/honour.svg");
        
        Result result = img.handle(context);
        assertThat(result.status().get()).isEqualTo(Status.OK);
        assertThat(result.contentType().get().subtype()).isEqualTo("svg+xml");
        assertThat(result.renderable().isPresent()).isTrue();
        assertThat(result.headers()).containsKey("mime-type");
    }

    @Test
    public void lastModified() {
        Route.Handler css = new AssetHandler(di).lastModified(true);
        
        Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.path()).thenReturn("/css/dummy.css");
        when(request.header(anyString())).thenReturn(Value.empty());
        
        Result result = css.handle(context);
        assertThat(result.headers()).containsKey("Last-Modified");
    }
    
    @Test
    public void etag() {
        Route.Handler css = new AssetHandler(di).etag(true);
        
        Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.path()).thenReturn("/css/dummy.css");
        when(request.header(anyString())).thenReturn(Value.empty());
        
        Result result = css.handle(context);
        assertThat(result.headers()).containsKey("ETag");
    }
    
    @Test
    public void maxAge() {
        Route.Handler css = new AssetHandler(di).maxAge(Assets.ONE_WEEK_SECONDS);
        
        Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.path()).thenReturn("/css/dummy.css");
        
        Result result = css.handle(context);
        assertThat(result.headers()).containsKey("Cache-Control");
    }
}
