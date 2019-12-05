package net.javapla.jawn.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Key;


public class InjectionTest {
    
    Injection injector;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        injector = mock(Injection.class);
        
        when(injector.require(any(Class.class))).thenCallRealMethod();
        when(injector.require(any(Class.class), anyString())).thenCallRealMethod();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requireClass() {
        injector.require(Value.class);
        verify(injector, times(1)).require(any(Key.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void requireClassAndName() {
        injector.require(Value.class, "named");
        verify(injector, times(1)).require(any(Key.class));
    }

}
