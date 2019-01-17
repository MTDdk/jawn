package net.javapla.jawn.core.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
    public void parsingString_should_workForNameAndValue() {
        assertEquals(Modes.PROD, Modes.determineModeFromString("prod"));
        assertEquals(Modes.PROD, Modes.determineModeFromString("PROD"));
        assertEquals(Modes.PROD, Modes.determineModeFromString(Constants.MODE_PRODUCTION));
        
        assertEquals(Modes.TEST, Modes.determineModeFromString("test"));
        assertEquals(Modes.TEST, Modes.determineModeFromString("TEST"));
        assertEquals(Modes.TEST, Modes.determineModeFromString(Constants.MODE_TEST));
        
        assertEquals(Modes.DEV, Modes.determineModeFromString("dev"));
        assertEquals(Modes.DEV, Modes.determineModeFromString("DEV"));
        assertEquals(Modes.DEV, Modes.determineModeFromString(Constants.MODE_DEVELOPMENT));
    }

}
