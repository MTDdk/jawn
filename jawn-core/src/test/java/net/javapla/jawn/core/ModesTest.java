package net.javapla.jawn.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.javapla.jawn.core.util.Constants;

public class ModesTest {

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
