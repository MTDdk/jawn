package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import net.javapla.jawn.core.util.ConvertUtil.ConversionException;

public class ConvertUtilTest {

    @Test
    public void toInteger_number() {
        Number n = Long.valueOf(300);
        assertThat(ConvertUtil.toInteger(n)).isEqualTo(300);
    }
    
    @Test
    public void toInteger_string() {
        String n = "303";
        assertThat(ConvertUtil.toInteger(n)).isEqualTo(303);
        
        n = "333.009";
        assertThat(ConvertUtil.toInteger(n)).isEqualTo(333);
    }
    
    @Test
    public void toInteger_object() {
        Object n = 304.01f;
        assertThat(ConvertUtil.toInteger(n)).isEqualTo(304);
    }
    
    @Test(expected = ConversionException.class)
    public void toInteger_null() {
        ConvertUtil.toInteger(null);
    }

    
    @Test
    public void toBoolean() {
        assertThat(ConvertUtil.toBoolean(true)).isTrue();
        assertThat(ConvertUtil.toBoolean("y")).isTrue();
        assertThat(ConvertUtil.toBoolean('y')).isTrue();
        assertThat(ConvertUtil.toBoolean('Y')).isTrue();
        assertThat(ConvertUtil.toBoolean("yes")).isTrue();
        assertThat(ConvertUtil.toBoolean("t")).isTrue();
        assertThat(ConvertUtil.toBoolean('t')).isTrue();
        assertThat(ConvertUtil.toBoolean('T')).isTrue();
        assertThat(ConvertUtil.toBoolean("true")).isTrue();
        assertThat(ConvertUtil.toBoolean("True")).isTrue();
        assertThat(ConvertUtil.toBoolean(1)).isTrue();
        assertThat(ConvertUtil.toBoolean(10)).isTrue();
        assertThat(ConvertUtil.toBoolean(2l)).isTrue();
        assertThat(ConvertUtil.toBoolean(BigInteger.TEN)).isTrue();
        assertThat(ConvertUtil.toBoolean(BigDecimal.ONE)).isTrue();
        
        assertThat(ConvertUtil.toBoolean(null)).isFalse();
        assertThat(ConvertUtil.toBoolean(false)).isFalse();
        assertThat(ConvertUtil.toBoolean("n")).isFalse();
        assertThat(ConvertUtil.toBoolean('n')).isFalse();
        assertThat(ConvertUtil.toBoolean('N')).isFalse();
        assertThat(ConvertUtil.toBoolean("no")).isFalse();
        assertThat(ConvertUtil.toBoolean('f')).isFalse();
        assertThat(ConvertUtil.toBoolean('F')).isFalse();
        assertThat(ConvertUtil.toBoolean("false")).isFalse();
        assertThat(ConvertUtil.toBoolean("False")).isFalse();
        assertThat(ConvertUtil.toBoolean(0)).isFalse();
        assertThat(ConvertUtil.toBoolean(-20)).isFalse();
        assertThat(ConvertUtil.toBoolean(-23l)).isFalse();
        assertThat(ConvertUtil.toBoolean(BigInteger.ZERO)).isFalse();
        assertThat(ConvertUtil.toBoolean(BigDecimal.valueOf(-2))).isFalse();
        
        assertThat(ConvertUtil.toBoolean("ye")).isFalse();
        assertThat(ConvertUtil.toBoolean("tru")).isFalse();
        assertThat(ConvertUtil.toBoolean("fal")).isFalse();
    }
}
