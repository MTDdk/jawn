package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Named;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.internal.TestHelper;
import net.javapla.jawn.core.mvc.Body;
import net.javapla.jawn.core.mvc.Param;
import net.javapla.jawn.core.mvc.PathParam;
import net.javapla.jawn.core.mvc.QueryParam;
import net.javapla.jawn.core.server.ServerRequest;

public class ActionParameterTest {

    private static Injector injector;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        injector = Guice.createInjector();
    }


    @Test
    public void stringParameter() throws NoSuchMethodException, SecurityException {
        Parameter param = firstParam("lookItUp");
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy");

        assertThat(actionParameter.optional).isFalse();
        assertThat(actionParameter.type).isEqualTo(String.class);
    }

    @Test
    public void optionalParameter() throws NoSuchMethodException, SecurityException {
        Parameter param = firstParam("lookItUp", Optional.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy-optional");

        assertThat(actionParameter.optional).isTrue();
        assertThat(actionParameter.type).isEqualTo(com.google.inject.util.Types.newParameterizedType(Optional.class, String.class));
    }

    @Test
    public void optionalParameter_with_missingValue() throws Throwable {
        Parameter param = firstParam("lookItUp", Optional.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy-optional");

        Context context = mock(Context.class);
        when(context.param(anyString())).thenReturn(Value.empty());

        Object result = actionParameter.value(context);

        assertThat(result).isNotNull(); // the returned must not be null, when the parameter is Optional
        assertThat(result).isInstanceOf(Optional.class);
    }

    @Test
    public void setParameter_with_missingValue() throws Throwable {
        String paramName = "lmgtfyset";

        Parameter param = firstParam("lookItUp", Set.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, paramName);

        Context context = mock(Context.class);
        when(context.param(paramName)).thenReturn(Value.empty());

        Object result = actionParameter.value(context);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Set.class);
    }

    @Test
    public void paramAsLong() throws Throwable {
        Parameter param = firstParam("lookItUp", long.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy_long");

        Value value = Value.of("400");

        Context context = mock(Context.class);
        when(context.param("lmgtfy_long")).thenReturn(value);

        Object result = actionParameter.value(context);
        assertThat(result).isInstanceOf(Long.class);
        assertThat(result).isEqualTo(400);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void convertToListValue() throws Throwable {
        Parameter param = firstParam("lookItUp", List.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "lmgtfy-list");

        Value value = Value.of("400", "cookie", "value");

        Context context = mock(Context.class);
        when(context.param("lmgtfy-list")).thenReturn(value);

        Object result = actionParameter.value(context);
        assertThat(result).isInstanceOf(List.class);
        assertThat((List<String>)result).containsExactly("400","cookie","value");
    }

    @Test
    public void requestParameter() throws Throwable {
        Parameter param = firstParam("lookItUp", Context.Request.class, Context.Response.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "req");

        Context.Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);

        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Context.Request.class);
        assertThat(result).isSameAs(request);
    }

    @Test
    public void responseParameter() throws Throwable {
        Parameter param = param("lookItUp", 1, Context.Request.class, Context.Response.class);//.getParameters()[1];
        ActionParameter actionParameter = new ActionParameter(param, "resp");

        Context.Response response = mock(Context.Response.class);
        Context context = mock(Context.class);
        when(context.resp()).thenReturn(response);

        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Context.Response.class);
        assertThat(result).isSameAs(response);
    }

    @Test
    public void bodyAsString() throws Throwable {
        Parameter param = firstParam("body");
        ActionParameter actionParameter = new ActionParameter(param, "b");

        String body = "some body";
        Context context = sendStringBody(body);


        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo(body);
    }

    @Test
    public void bodyAsLong() throws Throwable {
        Parameter param = firstParam("body", long.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "b");

        long value = 12445;
        Context context = sendStringBody(String.valueOf(value));


        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Long.class);
        assertThat(result).isEqualTo(value);
    }

    @Test(expected = Up.ParsableError.class)
    public void bodyParsingException() throws Throwable {
        // Tries to convert to long, but gets an unparsable string

        Parameter param = firstParam("body", long.class);//.getParameters()[0];
        ActionParameter actionParameter = new ActionParameter(param, "b");

        String body = "some body";
        Context context = sendStringBody(body);


        actionParameter.value(context);
    }

    @Test
    public void paramAsString() throws Throwable {
        Parameter param = firstParam("param");
        ActionParameter actionParameter = new ActionParameter(param, "b");

        Value value = Value.of("2015-03-14");

        Context context = mock(Context.class);
        when(context.param("b")).thenReturn(value);

        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo("2015-03-14");
    }

    @Test
    public void paramAsValue() throws Throwable {
        Parameter param = firstParam("paramValue", Value.class);
        ActionParameter actionParameter = new ActionParameter(param, "v");

        Value value = Value.of("2015-03-14");

        Context context = mock(Context.class);
        when(context.param("v")).thenReturn(value);

        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Value.class);
        assertThat(result).isEqualTo(value);
    }
    
    @Test
    public void pathParam() throws Throwable {
        Parameter param = firstParam("path", int.class);
        ActionParameter actionParameter = new ActionParameter(param, "id");

        Value value = Value.of("444");

        Context.Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.pathParam("id")).thenReturn(value);
        
        
        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Integer.class); // TODO I would *really* like this be int.class
        assertThat(result).isEqualTo(444);
        verify(context, never()).param(anyString());
    }
    
    @Test
    public void queryParam() throws Throwable {
        Parameter param = firstParam("query", Optional.class);
        ActionParameter actionParameter = new ActionParameter(param, "exists");

        Value value = Value.of("true");

        Context.Request request = mock(Context.Request.class);
        Context context = mock(Context.class);
        when(context.req()).thenReturn(request);
        when(request.queryParam("exists")).thenReturn(value);
        
        
        Object result = actionParameter.value(context);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Optional.class);
        assertThat(result).isEqualTo(Optional.of(true));
        
        verify(context, never()).param(anyString()); // only the request.queryParam should be called
    }
    


    public void lookItUp(@Named("lmgtfy") String s) {}
    public void lookItUp(@Named("lmgtfy-optional") Optional<String> s) {}
    public void lookItUp(long lmgtfy_long) {}
    public void lookItUp(@Named("lmgtfy-list") List<String> l) {}
    public void lookItUp(Set<String> lmgtfyset) {}
    public void lookItUp(Context.Request req, Context.Response resp) {}
    public void body(@Body String b) {}
    public void body(@Body long l) {}
    public void param(@Param("b") String b) {}
    public void paramValue(@Param("v") Value value) {}
    public void path(@PathParam("id") int value) {}
    public void query(@QueryParam("exists") Optional<Boolean> value) {}


    private static Parameter firstParam(String name) throws NoSuchMethodException, SecurityException {
        return firstParam(name, String.class);//.getParameters()[0];
    }

    private static Parameter firstParam(final String name, Class<?> ... parameters) throws NoSuchMethodException, SecurityException {
        return ActionParameterTest.class.getDeclaredMethod(name, parameters).getParameters()[0];
    }

    private static Parameter param(final String name, int index, Class<?> ... parameters) throws NoSuchMethodException, SecurityException {
        return ActionParameterTest.class.getDeclaredMethod(name, parameters).getParameters()[index];
    }

    private Context sendStringBody(String body) throws Exception {
        ServerRequest request = mock(ServerRequest.class);
        when(request.header("Content-Length")).thenReturn(Value.of(String.valueOf(body.length())));
        when(request.header("Content-Type")).thenReturn(Value.of(MediaType.TEXT.name()));
        when(request.bytes()).thenCallRealMethod();
        when(request.in()).thenReturn(new ByteArrayInputStream(body.getBytes()));

        Context context = TestHelper.contextImpl(request, null, StandardCharsets.UTF_8, null, injector);

        return context;
    }
}
