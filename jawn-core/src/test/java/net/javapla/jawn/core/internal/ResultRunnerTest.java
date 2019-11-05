package net.javapla.jawn.core.internal;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import com.google.inject.Injector;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.renderers.RendererEngine;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

public class ResultRunnerTest {

    @Test
    public void execute() {
        ServerResponse response = mock(ServerResponse.class);
        MediaType mediaType = MediaType.valueOf("test/test");
        ContextImpl context = TestHelper.contextImpl(mock(ServerRequest.class), response, StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        RendererEngineOrchestrator engine = mock(RendererEngineOrchestrator.class);
        doAnswer(AdditionalAnswers.answerVoid((MediaType type, Consumer<RendererEngine> c) -> {
            RendererEngine e = mock(RendererEngine.class);
            c.accept(e);
            verify(e, times(1)).invoke(any(Context.class), any(Object.class));
        })).when(engine).getRendererEngineForContentType(eq(mediaType), any());

        
        ResultRunner runner = new ResultRunner(engine);
        
        Result result = Results.ok("test")
            .contentType(mediaType);
        
        runner.execute(result, context);
        verify(response, times(1)).end();
    }

    @Test
    public void execute_without_MediaType() {
        ServerResponse response = mock(ServerResponse.class);
        
        ContextImpl context = TestHelper.contextImpl(mock(ServerRequest.class), response, StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        RendererEngineOrchestrator engine = mock(RendererEngineOrchestrator.class);
        ResultRunner runner = new ResultRunner(engine);
        
        Result result = Results.ok("test")
            .contentType((MediaType)null);
        
        assertThrows(Up.class, () -> runner.execute(result, context));
        verify(response, times(0)).end();
    }
    
    @Test
    public void execute_HEAD() throws Exception {
        ServerRequest request = mock(ServerRequest.class);
        when(request.method()).thenReturn(HttpMethod.HEAD);
        ServerResponse response = mock(ServerResponse.class);
        
        ContextImpl context = TestHelper.contextImpl(request, response, StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        RendererEngineOrchestrator engine = mock(RendererEngineOrchestrator.class);
        
        ResultRunner runner = new ResultRunner(engine);
        
        Result result = Results.ok("test");
        
        runner.execute(result, context);
        
        verify(response, times(1)).end();
        verify(engine, times(0)).getRendererEngineForContentType(any(MediaType.class), any());
    }
    
    @Test
    public void invoke_throws() throws Exception {
        ServerResponse response = mock(ServerResponse.class);
        MediaType mediaType = MediaType.valueOf("test/test");
        ContextImpl context = TestHelper.contextImpl(mock(ServerRequest.class), response, StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        RendererEngine e = mock(RendererEngine.class);
        doThrow(IOException.class).when(e).invoke(any(), any());
        
        RendererEngineOrchestrator engine = mock(RendererEngineOrchestrator.class);
        doAnswer(AdditionalAnswers.answerVoid((MediaType type, Consumer<RendererEngine> c) -> {
            c.accept(e);
        })).when(engine).getRendererEngineForContentType(eq(mediaType), any());

        
        ResultRunner runner = new ResultRunner(engine);
        
        Result result = Results.ok("test")
            .contentType(mediaType);
        
        assertThrows(Up.RenderableError.class, () -> runner.execute(result, context));
        verify(response, times(0)).end();
    }
    
    @Test
    public void invoke_throws_Up() throws Exception {
        ServerResponse response = mock(ServerResponse.class);
        MediaType mediaType = MediaType.valueOf("test/test");
        ContextImpl context = TestHelper.contextImpl(mock(ServerRequest.class), response, StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        RendererEngine e = mock(RendererEngine.class);
        doThrow(Up.IO.class).when(e).invoke(any(), any());
        
        RendererEngineOrchestrator engine = mock(RendererEngineOrchestrator.class);
        doAnswer(AdditionalAnswers.answerVoid((MediaType type, Consumer<RendererEngine> c) -> {
            c.accept(e);
        })).when(engine).getRendererEngineForContentType(eq(mediaType), any());

        
        ResultRunner runner = new ResultRunner(engine);
        
        Result result = Results.ok("test")
            .contentType(mediaType);
        
        assertThrows(Up.IO.class, () -> runner.execute(result, context));
        verify(response, times(0)).end();
    }
}
