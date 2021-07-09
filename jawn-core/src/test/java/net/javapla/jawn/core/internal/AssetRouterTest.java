package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.truth.Correspondence;

import net.javapla.jawn.core.Assets;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Builder;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;

public class AssetRouterTest {
    
    private static final RendererEngineOrchestrator RENDERERS = mock(RendererEngineOrchestrator.class);
    private static DeploymentInfo di;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        di = mock(DeploymentInfo.class);
        when(di.listResources(eq(""))).thenReturn(Stream.of("/img", "js", "css", "favicon.ico", "favicon-red.ico"));
    }

    @Test
    public void readAssetFolders() {
        @SuppressWarnings("unchecked")
        List<Builder> assets = AssetRouter.assets(di, new Assets.Impl(), mock(BiConsumer.class));
        assertThat(assets).hasSize(5);
        
        assertThat(assets.stream().map(bob -> bob.build(RENDERERS)).collect(Collectors.toList()))
            .comparingElementsUsing(Correspondence.from((Route route, String expected) -> route.path().startsWith(expected), "starts with"))
            .containsExactly("/img/","/js/", "/css/", "/favicon.ico", "/favicon-red.ico");
    }
    
    
}
