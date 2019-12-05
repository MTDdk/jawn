package net.javapla.jawn.core.renderers;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Module;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;
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
    public void contentTypes() {
        assertThat(engine.getContentTypes()).containsAllOf(MediaType.TEXT, MediaType.PLAIN, MediaType.XML, MediaType.JSON);
    }
    
    @Test
    public void shouldThrow_when_ridiculousMediaType() {
        assertThrows(Up.BadMediaType.class, () -> engine.getRendererEngineForContentType(MediaType.valueOf("test/test"), engine -> {}));
    }
    
    @Test
    public void logEngines() {
        StringBuilder bob = new StringBuilder();
        
        ((RendererEngineOrchestratorImpl)engine).logEngines(bob::append);
        
        bob.trimToSize();
        String result = bob.toString();
        
        assertThat(result).contains("Registered template engines");
        assertThat(result).contains(MediaType.TEXT.name());
        assertThat(result).contains(MediaType.PLAIN.name());
        assertThat(result).contains(MediaType.XML.name());
        assertThat(result).contains(MediaType.JSON.name());
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
        AtomicBoolean executed = new AtomicBoolean(false);
        Context context = mock(Context.class);
        
        RendererEngineOrchestrator orchestrator = engine(binder -> {
            binder.bind(RendererEngine.class).toInstance(new RendererEngine() {
                public void invoke(Context context, Object renderable) throws Exception { executed.setPlain(true); }
                public MediaType[] getContentType() { return new MediaType[] { MediaType.valueOf("test/test") }; }
            });
        });
        
        
        orchestrator.getRendererEngineForContentType(MediaType.valueOf("test/test"), engine -> {
            try { engine.invoke(context, new Object()); } catch (Exception e) { fail(); }
        });
        
        assertThat(executed.getPlain()).isTrue();
    }
    
    private static RendererEngineOrchestrator engine(Module ... modules) {
        return Guice.createInjector(modules).getInstance(RendererEngineOrchestrator.class);
    }
}