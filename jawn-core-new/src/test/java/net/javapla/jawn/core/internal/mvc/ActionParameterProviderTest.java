package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.internal.reflection.ClassMeta;

public class ActionParameterProviderTest {

    private static ActionParameterProvider provider;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        provider = new ActionParameterProvider(new ClassMeta());
    }

    @Test
    public void named() throws NoSuchMethodException, SecurityException {
        assertThat(ActionParameterProvider.nameFor( firstParam("testMethodFTW") )).isEqualTo("actionParameterOfAwesome");
        assertThat(ActionParameterProvider.nameFor( firstParam("testMethodFTW_guice") )).isEqualTo("actionParameterOfAwesome");
    }
    
    @Test
    public void unnamed_shouldBe_unparsable() throws NoSuchMethodException, SecurityException {
        assertThat(ActionParameterProvider.nameFor( firstParam("testMethod") )).isNull();
    }
    
    @Test
    public void unnamed() throws NoSuchMethodException, SecurityException {
        assertThat(provider.name( firstParam("testMethod") )).isEqualTo("actionParameter");
    }
    
    @Test
    public void unnamedMultiple() throws NoSuchMethodException, SecurityException {
        Method action = action("testMethod", String.class, Context.class, Integer.class);
        Parameter[] parameters = action.getParameters();
        
        assertThat(provider.name( parameters[0] )).isEqualTo("actionParameter");
        assertThat(provider.name( parameters[1] )).isEqualTo("actionContext");
        assertThat(provider.name( parameters[2] )).isEqualTo("actionValue");
    }
    
    @Test
    public void listParameters() throws NoSuchMethodException, SecurityException {
        Method action = action("testMethod", String.class, Context.class, Integer.class);
        
        List<ActionParameter> list = provider.parameters(action);
        assertThat(list).hasSize(3);
        assertThat(list.get(0).name).isEqualTo("actionParameter");
        assertThat(list.get(1).name).isEqualTo("actionContext");
        assertThat(list.get(2).name).isEqualTo("actionValue");
    }
    
    @Test
    public void listNameParameter() throws NoSuchMethodException, SecurityException {
        Method action = action("testMethodFTW", String.class);
        
        List<ActionParameter> list = provider.parameters(action);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).name).isEqualTo("actionParameterOfAwesome");
    }
    
    // this is what we are looking for
    public void testMethodFTW_guice(@com.google.inject.name.Named("actionParameterOfAwesome") final String s) { }
    public void testMethodFTW(@javax.inject.Named("actionParameterOfAwesome") final String s) { }
    public void testMethod(String actionParameter) {}
    public void testMethod(final String actionParameter, final Context actionContext, final Integer actionValue) {}
    
    private static Parameter firstParam(String name) throws NoSuchMethodException, SecurityException {
        return action(name, String.class).getParameters()[0];
    }
    
    private static Method action(final String name, Class<?> ... parameters) throws NoSuchMethodException, SecurityException {
        return ActionParameterProviderTest.class.getDeclaredMethod(name, parameters);
    }
}
