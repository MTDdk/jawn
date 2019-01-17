package net.javapla.jawn.core.configuration;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.Test;

public class DeploymentInfoTest {

    private final static String context = "/contextpath";
    
    static DeploymentInfo noContext;
    static DeploymentInfo withContext;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        noContext = new DeploymentInfo(mock(JawnConfigurations.class), "");
        withContext = new DeploymentInfo(mock(JawnConfigurations.class), context);
    }
    
    @Test
    public void noStrippingNeeded() {
        String requestPath = "/path/to/glory";
        
        assertThat(DeploymentInfo.stripContextPath("", requestPath),is(equalTo(requestPath)));
        assertThat(DeploymentInfo.stripContextPath("/different", requestPath),is(equalTo(requestPath)));
        assertThat(DeploymentInfo.stripContextPath("completely/different", requestPath),is(equalTo(requestPath)));
    }

    @Test
    public void strippingContextPath() {
        String requestPath = "/path/to/glory";
        
        String context = "/context";
        assertThat(DeploymentInfo.stripContextPath(context, context + requestPath),is(equalTo(requestPath)));
        
        context = "/context/more";
        assertThat(DeploymentInfo.stripContextPath(context, context + requestPath),is(equalTo(requestPath)));
    }

    @Test
    public void noContextStripContextPath() {
        assertThat(noContext.stripContextPath("/some/path"),is(equalTo("/some/path")));
        assertThat(noContext.stripContextPath("/path"),is(equalTo("/path")));
        assertThat(noContext.stripContextPath("/notcontext/some/path"),is(equalTo("/notcontext/some/path")));
        assertThat(noContext.stripContextPath(context + "/some/path"),is(equalTo(context + "/some/path")));
    }
    
    @Test
    public void withContextStripContextPath() {
        assertThat(withContext.stripContextPath("/some/path"),is(equalTo("/some/path")));
        assertThat(withContext.stripContextPath("/notcontext/some/path"),is(equalTo("/notcontext/some/path")));
        assertThat(withContext.stripContextPath(context + "/some/path"),is(equalTo("/some/path")));
    }
    
    @Test
    public void noContextTranslate() {
        assertThat(noContext.translateIntoContextPath("/some/path"),is(equalTo("/some/path")));
        assertThat(noContext.translateIntoContextPath("/path"),is(equalTo("/path")));
        assertThat(noContext.translateIntoContextPath("/notcontext/some/path"),is(equalTo("/notcontext/some/path")));
        assertThat(noContext.translateIntoContextPath(context + "/some/path"),is(equalTo(context + "/some/path")));
    }
    
    @Test
    public void withContextTranslate() {
        assertThat(withContext.translateIntoContextPath("/some/path"),is(equalTo(context + "/some/path")));
        assertThat(withContext.translateIntoContextPath("/path"),is(equalTo(context + "/path")));
        assertThat(withContext.translateIntoContextPath("/path?v=1234567890123"),is(equalTo(context + "/path?v=1234567890123")));
        assertThat(withContext.translateIntoContextPath("different/some/path"),is(equalTo(context + "/different/some/path")));
    }

}
