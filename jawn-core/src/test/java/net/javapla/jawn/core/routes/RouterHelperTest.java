package net.javapla.jawn.core.routes;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.Constants;

public class RouterHelperTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty(Constants.SYSTEM_PROPERTY_APPLICATION_BASE_PACKAGE, Constants.APPLICATION_STANDARD_PACKAGE);
    }

    @Test
    public void generate_should_encode() {
        assertEquals("/test?%23=anchor&upload=true", RouterHelper.generate("test", null, null, CollectionUtil.map("#","anchor","upload", "true")));
        assertEquals("/test?%23=anchor&upload=true", RouterHelper.generate("/test", null, null, CollectionUtil.map("#","anchor","upload", "true")));
        assertEquals("/test", RouterHelper.generate("/test", null, null, new HashMap<String,String>(0)));
        assertEquals("/test?%25=+&%C3%B8=%26",RouterHelper.generate("/test", null, null, CollectionUtil.map("%"," ","ø", "&")));
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
    
    @Test
    public void reverseRoute_should_keepPackagesAndUnderscore() {
        String reverseRoute = RouterHelper.getReverseRouteFast(app.controllers.testing.more.CakeFrostingsController.class);
        assertEquals("/testing/more/cake_frostings", reverseRoute);
    }

}
