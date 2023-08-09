package net.javapla.jawn.core.internal.template.lox;

import org.junit.jupiter.api.Test;

class LoxTest {

    @Test
    void test() {
        Lox.run("var language = \"lox\";");
        
        Lox.run(""
            + "print \"one\";"
            + "print true;"
            + "print 2 + 1;");
    }

    @Test
    void ast() {
        Expr expression = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(123)),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(
                new Expr.Literal(45.67)));
        
        System.out.println(new AstPrinter().print(expression));
    }
}
