package net.javapla.jawn.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimeUtilTest {

    @Test
    public void minutes_should_parseToSeconds() {
        int seconds = TimeUtil.seconds("3m");
        int correct = 3 * 60;
        assertEquals(correct, seconds);
    }
    
    @Test
    public void seconds_should_parseToSeconds() {
        int seconds = TimeUtil.seconds("6s");
        int correct = 6;
        assertEquals(correct, seconds);
    }
    
    @Test
    public void hours_should_parseToSeconds() {
        int seconds = TimeUtil.seconds("6h");
        int correct = 6 * 60 * 60;
        assertEquals(correct, seconds);
    }
    
    @Test
    public void days_should_parseToSeconds() {
        int seconds = TimeUtil.seconds("2d");
        int correct = 2 * 24 * 60 * 60;
        assertEquals(correct, seconds);
    }
    
    @Test
    public void nothing_should_parseTo30m() {
        int seconds = TimeUtil.seconds("");
        int correct = 30 * 60;
        assertEquals(correct, seconds);
        
        seconds = TimeUtil.seconds(null);
        assertEquals(correct, seconds);
    }
    
    @Test
    public void unknown_should_parseTo30m() {
        int seconds = TimeUtil.seconds("unknown");
        int correct = 30 * 60;
        assertEquals(correct, seconds);
    }
    
    @Test
    public void malformedInput_should_parseTo30m() {
        int seconds = TimeUtil.seconds("77i7s");
        int correct = 30 * 60;
        assertEquals(correct, seconds);
    }

    @Test
    public void mixtureOfInputs() {
        int seconds = TimeUtil.seconds("200m1s");
        int correct = 200 * 60 + 1;
        assertEquals(correct, seconds);
    }
    
    @Test
    public void reverse_should_fail() {
        int seconds = TimeUtil.seconds("m77s7");
        int correct = 30 * 60;
        assertEquals(correct, seconds);
    }
}
