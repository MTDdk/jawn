package net.javapla.jawn.core.internal.mvc;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Parameter;

import org.junit.Test;


public class ActionParameterNameTest {

    @Test
    public void named() throws NoSuchMethodException, SecurityException {
        assertThat(ActionParameterName.nameFor( firstParam("testMethodFTW") )).isEqualTo("actionParameterOfAwesome");
        assertThat(ActionParameterName.nameFor( firstParam("testMethodFTW_guice") )).isEqualTo("actionParameterOfAwesome");
    }
    
    @Test
    public void unnamed_shouldBe_unparsable() throws NoSuchMethodException, SecurityException {
        assertThat(ActionParameterName.nameFor( firstParam("testMethod") )).isNull();
    }
    
    
    // this is what we are looking for
    public void testMethodFTW_guice(@com.google.inject.name.Named("actionParameterOfAwesome") final String actionParameterOfAwesome) { }
    public void testMethodFTW(@javax.inject.Named("actionParameterOfAwesome") final String actionParameterOfAwesome) { }
    public void testMethod(String actionParameter) {}
    
    public static Parameter firstParam(String name) throws NoSuchMethodException, SecurityException {
        return ActionParameterNameTest.class.getDeclaredMethod(name, String.class).getParameters()[0];
    }
}
