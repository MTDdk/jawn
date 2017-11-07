package net.javapla.jawn.core.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class StringUtilTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void underscore() {
        assertEquals("alice_in_wonderland", StringUtil.underscore("AliceInWonderland"));
        assertEquals("alice_in_wonderland", StringUtil.underscore("aliceInWonderland"));
        assertEquals("alice_in_wonder_land", StringUtil.underscore("aliceInWonderLand"));
        assertEquals("alice_in_wonder_lan_d", StringUtil.underscore("aliceInWonderLanD"));
    }

    @Test
    public void decapitalize() {
        assertEquals("alice", StringUtil.decapitalize("Alice"));
        assertEquals("aliceIn", StringUtil.decapitalize("AliceIn"));
        assertEquals("aliceInWonderland", StringUtil.decapitalize("AliceInWonderland"));
    }
    
}
