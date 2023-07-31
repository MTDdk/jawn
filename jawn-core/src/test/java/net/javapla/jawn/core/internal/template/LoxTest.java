package net.javapla.jawn.core.internal.template;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LoxTest {

    @Test
    void test() {
        String s = "<html>{{attribute}}</html>";
        Lox.run(s);
    }

}
