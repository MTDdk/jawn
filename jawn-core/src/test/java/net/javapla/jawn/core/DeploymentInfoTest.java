package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import net.javapla.jawn.core.util.Constants;

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
        
        assertThat(di.getRealPath("start_of_path/real.jpg")).isEqualTo("webapp/real.jpg");
        assertThat(di.getRealPath("/start_of_path/real.jpg")).isEqualTo("webapp/real.jpg");
    }

    @Test
    public void realPath_without_context() {
        Config config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of("webapp/directory"));
        DeploymentInfo di = new DeploymentInfo(config, "");
        
        assertThat(di.getRealPath("/img/real.jpg")).isEqualTo("webapp/directory/img/real.jpg");
        assertThat(di.getRealPath("img/real.jpg")).isEqualTo("webapp/directory/img/real.jpg");
    }
    
    @Test
    public void strip() {
        String context = "/start_of_path";
        
        Config config = mock(Config.class);
        DeploymentInfo di = new DeploymentInfo(config, context);
        
        assertThat(di.stripContextPath("/start_of_path/img/some.jpg")).isEqualTo("/img/some.jpg");
        assertThat(di.stripContextPath("/img/some.jpg")).isEqualTo("/img/some.jpg");
    }
}
