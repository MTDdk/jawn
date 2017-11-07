package net.javapla.jawn.core.routes;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.Constants;

public class RouterHelperTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(Constants.SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE, "app");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void generate_should_encode() {
        String string = RouterHelper.generate("test", null, null, CollectionUtil.map("#","anchor","upload", "true"));
        assertEquals("/test?%23=anchor&upload=true", string);
    }
    
    @Test
    public void reverseRoute_should_stripController() {
        String reverseRoute = RouterHelper.getReverseRouteFast(app.controllers.MockController.class);
        assertEquals("/mock", reverseRoute);
    }
    
    @Test
    public void reverseRoute_should_keepAdditionalPackageStructure() {
        String reverseRoute = RouterHelper.getReverseRouteFast(app.controllers.testing.more.CakeController.class);
        assertEquals("/testing/more/cake", reverseRoute);
    }

}
