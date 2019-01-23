package net.javapla.jawn.core.renderers;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Module;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;

public class RendererEngineOrchestratorTest {
    
    private static RendererEngineOrchestrator engine;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        engine = engine();
    }

    @Test
    public void unknownMediaType() {
        assertThat(engine.hasRendererEngineForContentType(MediaType.valueOf("test/test"))).isFalse();
    }
    
    @Test
    public void knownMediaType() {
        assertThat(engine.hasRendererEngineForContentType(MediaType.TEXT)).isTrue();
        assertThat(engine.hasRendererEngineForContentType(MediaType.PLAIN)).isTrue();
        assertThat(engine.hasRendererEngineForContentType(MediaType.XML)).isTrue();
    }
    
    @Test
    public void rendererGetsRegistered() {
        
        RendererEngineOrchestrator orchestrator = engine(binder -> {
            binder.bind(RendererEngine.class).toInstance(new RendererEngine() {
                public void invoke(Context context, Object renderable) throws Exception { }
                public MediaType[] getContentType() { return new MediaType[] { MediaType.valueOf("test/test") }; }
            });
        });
        
        boolean hasRendererEngineForContentType = orchestrator.hasRendererEngineForContentType(MediaType.valueOf("test/test"));
        
        assertThat(hasRendererEngineForContentType).isTrue();
    }

    @Test
    public void rendererExecutes() {
        boolean[] executed = new boolean[1];
        Context context = mock(Context.class);
        
        RendererEngineOrchestrator orchestrator = engine(binder -> {
            binder.bind(RendererEngine.class).toInstance(new RendererEngine() {
                public void invoke(Context context, Object renderable) throws Exception { executed[0] = true; }
                public MediaType[] getContentType() { return new MediaType[] { MediaType.valueOf("test/test") }; }
            });
        });
        
        
        orchestrator.getRendererEngineForContentType(MediaType.valueOf("test/test"), engine -> {
            try { engine.invoke(context, new Object()); } catch (Exception e) { fail(); }
        });
        
        assertThat(executed[0]).isTrue();
    }
    
    private static RendererEngineOrchestrator engine(Module ... modules) {
        return Guice.createInjector(modules).getInstance(RendererEngineOrchestrator.class);
    }
}
