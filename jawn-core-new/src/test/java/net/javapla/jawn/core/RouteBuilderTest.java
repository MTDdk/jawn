package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RouteBuilderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test(expected = IllegalArgumentException.class)
    public void emptyPath() {
        new Route.Builder(HttpMethod.GET).path("");
    }
    
    @Test(expected = NullPointerException.class)
    public void nullPath() {
        String s = null;
        new Route.Builder(HttpMethod.GET).path(s);
    }

}
