package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import com.google.inject.Key;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.Path;

public class MvcMethodHandlerTest {


    @Test
    public void noParameters() {
        Context context = mock(Context.class);
        when(context.require(Key.get(SingleRoute.class))).thenReturn(new SingleRoute());
        
        MvcRouter.methods(SingleRoute.class, methods -> {
            MvcMethodHandler handler = new MvcMethodHandler( methods[0], SingleRoute.class);
            Result result = handler.handle(context);
            assertThat(result.renderable().get()).isEqualTo("test".getBytes());//Strings get converted to byte[] in Results.text
        });
    }
    
    @Test
    public void contextParameter() {
        // setup
        Context context = mock(Context.class);
        when(context.require(Key.get(MethodParams.class))).thenReturn(new MethodParams());
        when(context.attribute(anyString())).thenReturn(Optional.of("test"));
        
        @SuppressWarnings("unchecked")
        Consumer<Method[]> c = mock(Consumer.class);
        
        doAnswer(AdditionalAnswers.answerVoid((Method[] methods) -> {
            // the actual work
            MvcMethodHandler handler = new MvcMethodHandler( methods[0], MethodParams.class);
            Result result = handler.handle(context);
            assertThat(result).isNotNull();
            assertThat(result.renderable().get()).isEqualTo("testtest".getBytes());
        })).when(c).accept(any(Method[].class));
        
        
        // execute
        MvcRouter.methods(MethodParams.class, c);
        
        // verify
        verify(c, times(1)).accept(any(Method[].class));
    }

    
/**** TEST CLASSES ****/
    
    @Path("/single")
    static class SingleRoute {
        @GET
        public Result test() {
            return Results.text("test");
        }
    }
    
    @Path("/methodparams")
    static class MethodParams {
        @GET
        public Result test(Context context) {
            return Results.text(context.attribute("").map(att -> att + "test").orElse("nothing"));
        }
    }
}
