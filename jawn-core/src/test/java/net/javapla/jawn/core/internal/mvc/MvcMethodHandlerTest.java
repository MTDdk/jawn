package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.internal.reflection.ClassMeta;
import net.javapla.jawn.core.mvc.GET;
import net.javapla.jawn.core.mvc.Path;

public class MvcMethodHandlerTest {


    @Test
    public void noParameters() {
        Injector injector = mock(Injector.class);
        when(injector.getProvider(Key.get(SingleRoute.class))).thenReturn((Provider<SingleRoute>) () -> new SingleRoute());
        
        Context context = mock(Context.class);
        
        MvcRouter.methods(SingleRoute.class, methods -> {
            MvcMethodHandler handler = new MvcMethodHandler( methods[0], SingleRoute.class, mock(ActionParameterProvider.class), injector);
            Result result = handler.handle(context);
            assertThat(result.renderable().get()).isEqualTo("test".getBytes());//Strings get converted to byte[] in Results.text
        });
    }
    
    @Test
    public void contextParameter() {
        // setup
        ActionParameterProvider actionParameterProvider = new ActionParameterProvider(new ClassMeta());
        
        Injector injector = mock(Injector.class);
        when(injector.getProvider(Key.get(MethodParams.class))).thenReturn((Provider<MethodParams>) () -> new MethodParams());
        
        Context context = mock(Context.class);
        when(context.attribute("testAttr")).thenReturn(Optional.of("test"));
        
        @SuppressWarnings("unchecked")
        Consumer<Method[]> callback = mock(Consumer.class);
        
        
        doAnswer(AdditionalAnswers.answerVoid((Method[] methods) -> {
            
            // the actual work
            MvcMethodHandler handler = new MvcMethodHandler( methods[0], MethodParams.class, actionParameterProvider, injector);
            Result result = handler.handle(context);
            assertThat(result).isNotNull();
            assertThat(result.renderable().get()).isEqualTo("testtest".getBytes());
            
        })).when(callback).accept(any(Method[].class));
        
        
        // execute
        MvcRouter.methods(MethodParams.class, callback);
        
        // verify
        verify(callback, times(1)).accept(any(Method[].class));
    }
    
    @Test
    public void stringParameter() {
        // setup
        ActionParameterProvider actionParameterProvider = new ActionParameterProvider(new ClassMeta());
        
        Injector injector = mock(Injector.class);
        when(injector.getProvider(Key.get(StringParameter.class))).thenReturn((Provider<StringParameter>) () -> new StringParameter());
        
        Context context = mock(Context.class);
        when(context.param("param")).thenReturn(Value.of("stringParameter"));
        
        @SuppressWarnings("unchecked")
        Consumer<Method[]> callback = mock(Consumer.class);
        
        
        doAnswer(AdditionalAnswers.answerVoid((Method[] methods) -> {
            
            // the actual work
            MvcMethodHandler handler = new MvcMethodHandler( methods[0], StringParameter.class, actionParameterProvider, injector);
            Result result = handler.handle(context);
            assertThat(result).isNotNull();
            assertThat(result.renderable().get()).isEqualTo("stringParameter".getBytes());
            
        })).when(callback).accept(any(Method[].class));
        
        
        // execute
        MvcRouter.methods(StringParameter.class, callback);
        
        // verify
        verify(callback, times(1)).accept(any(Method[].class));
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
            // appends "test" onto the result from context.attribute()
            return Results.text(context.attribute("testAttr").map(att -> att + "test").orElse("nothing"));
        }
    }
    
    @Path("/string")
    static class StringParameter {
        @GET
        public Result test(Optional<String> param) {
            return Results.text(param.get());
        }
    }
}
