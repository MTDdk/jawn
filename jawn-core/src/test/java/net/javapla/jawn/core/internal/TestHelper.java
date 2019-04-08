package net.javapla.jawn.core.internal;

import java.nio.charset.Charset;

import com.google.inject.Injector;

import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

public class TestHelper {

    public static ContextImpl contextImpl(final ServerRequest sreq, final ServerResponse resp, final Charset charset, final DeploymentInfo deploymentInfo, final Injector injector) {
        return new ContextImpl(sreq, resp, charset, deploymentInfo, injector);
    }
}
