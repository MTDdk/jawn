package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.truth.Correspondence;

import net.javapla.jawn.core.Assets;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Builder;

public class AssetRouterTest {
    
    private static String resources = Paths.get("src", "test", "resources", "webapp").toString();
    private static DeploymentInfo di;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        di = mock(DeploymentInfo.class);
        when(di.getRealPath("")).thenReturn(resources);
    }

    @Test
    public void readAssetFolders() {
        List<Builder> assets = AssetRouter.assets(di, new Assets.Impl());
        assertThat(assets).hasSize(5);
        
        assertThat(assets.stream().map(Route.Builder::build).collect(Collectors.toList()))
            .comparingElementsUsing(Correspondence.from((Route route, String expected) -> route.path().startsWith(expected), "starts with"))
            .containsExactly("/img/","/js/", "/css/", "/favicon.ico", "/favicon-red.ico");
    }
    
    
}
