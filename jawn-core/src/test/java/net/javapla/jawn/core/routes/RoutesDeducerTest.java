package net.javapla.jawn.core.routes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.FiltersHandler;
import net.javapla.jawn.core.http.HttpMethod;
import net.javapla.jawn.core.reflection.ActionInvoker;
import net.javapla.jawn.core.reflection.RoutesDeducer;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.PropertiesConstants;

public class RoutesDeducerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(Constants.SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE, Constants.APPLICATION_STANDARD_PACKAGE);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void test() {
        RoutesDeducer deducer = new RoutesDeducer(mock(FiltersHandler.class), mock(ActionInvoker.class));
        RoutesDeducer controllers = deducer.deduceRoutesFromControllers(PropertiesConstants.CONTROLLER_PACKAGE);
        
        RouteTrie trie = controllers.getRoutes();
        assertNotNull(trie.findExact("/mock", HttpMethod.GET));
        assertNotNull(trie.findExact("/unit_test", HttpMethod.GET));
        assertNotNull(trie.findExact("/unit_test/longer_action", HttpMethod.GET));
        assertNotNull(trie.findExact("/unit_test/simple", HttpMethod.GET));
        assertNotNull(trie.findExact("/unit_test/simple", HttpMethod.POST));
    }

}
