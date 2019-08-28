package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Test;

import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StreamUtil;

public class DeploymentInfoTest {

    @Test
    public void contextPath() {
        String context = "/start_of_path";
        
        Config config = mock(Config.class);
        DeploymentInfo di = new DeploymentInfo(config, context);
        
        assertThat(di.getContextPath()).isEqualTo(context);
        assertThat(di.translateIntoContextPath("/img/some.jpg")).isEqualTo("/start_of_path/img/some.jpg");
        assertThat(di.translateIntoContextPath("img/some.jpg")).isEqualTo("/start_of_path/img/some.jpg");
    }
    
    @Test
    public void realPath_with_context() {
        String context = "start_of_path/";
        
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of("webapp"));
        DeploymentInfo di = new DeploymentInfo(config, context);
        
        assertThat(di.getRealPath("start_of_path/real.jpg").toString()).isEqualTo("webapp/real.jpg");
        assertThat(di.getRealPath("/start_of_path/real.jpg").toString()).isEqualTo("webapp/real.jpg");
    }

    @Test
    public void realPath_without_context() {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of("webapp/directory"));
        DeploymentInfo di = new DeploymentInfo(config, "");
        
        assertThat(di.getRealPath("/img/real.jpg").toString()).isEqualTo("webapp/directory/img/real.jpg");
        assertThat(di.getRealPath("img/real.jpg").toString()).isEqualTo("webapp/directory/img/real.jpg");
    }
    
    @Test
    public void strip() {
        String context = "/start_of_path";
        
        Config config = mock(Config.class);
        DeploymentInfo di = new DeploymentInfo(config, context);
        
        assertThat(di.stripContextPath("/start_of_path/img/some.jpg")).isEqualTo("/img/some.jpg");
        assertThat(di.stripContextPath("/img/some.jpg")).isEqualTo("/img/some.jpg");
    }
    
    @Test
    public void asResource() throws IOException {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(Paths.get("src", "test", "resources", "webapp").toString()));
        DeploymentInfo di = new DeploymentInfo(config, "");
        
        InputStream input = di.resourceAsStream("css/dummy.css");
        String read = StreamUtil.read(input);
        assertThat(read).isEqualTo("body { height: 73px; }");
    }
    
    @Test (expected = NoSuchFileException.class)
    public void missingResource() throws IOException {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(Paths.get("src", "test", "resources", "webapp").toString()));
        DeploymentInfo di = new DeploymentInfo(config, "");
        
        di.resourceAsStream("css/unavailable.css"); // should throw
    }
    
    @Test
    public void resourceExists() {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(Paths.get("src", "test", "resources", "webapp").toString()));
        DeploymentInfo di = new DeploymentInfo(config, "");
        
        assertThat(di.resourceExists("css/dummy.css")).isTrue();
    }
    
    @Test
    public void resourceNotExists() {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(Paths.get("src", "test", "resources", "webapp").toString()));
        DeploymentInfo di = new DeploymentInfo(config, "");
        
        assertThat(di.resourceExists("css/unavailable.css")).isFalse();
    }
}
