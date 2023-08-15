package net.javapla.jawn.core.internal.template.lox;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LoxEnvironmentTest {

    @Test
    void test() {
        String source = ""
            + "var a = \"global a\";\n"
            + "var b = \"global b\";\n"
            + "var c = \"global c\";\n"
            + "{\n"
            + "  var a = \"outer a\";\n"
            + "  var b = \"outer b\";\n"
            + "  {\n"
            + "    var a = \"inner a\";\n"
            + "    print a;\n"
            + "    print b;\n"
            + "    print c;\n"
            + "  }\n"
            + "  print a;\n"
            + "  print b;\n"
            + "  print c;\n"
            + "}\n"
            + "print a;\n"
            + "print b;\n"
            + "print c;";
        
        Lox.run(source);
    }

}
