package net.javapla.jawn.core.internal.template.lox;

import net.javapla.jawn.core.internal.template.lox.Expr.Assign;
import net.javapla.jawn.core.internal.template.lox.Expr.Binary;
import net.javapla.jawn.core.internal.template.lox.Expr.Grouping;
import net.javapla.jawn.core.internal.template.lox.Expr.Literal;
import net.javapla.jawn.core.internal.template.lox.Expr.This;
import net.javapla.jawn.core.internal.template.lox.Expr.Unary;
import net.javapla.jawn.core.internal.template.lox.Expr.Variable;

class AstPrinter implements Expr.Visitor<String> {
    
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssign(Assign expr) {
        return null;
    }

    @Override
    public String visitBinary(Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGrouping(Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteral(Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitThis(This expr) {
        return null;
    }

    @Override
    public String visitUnary(Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }
    
    @Override
    public String visitVariable(Variable variable) {
        return null;
    }

    private String parenthesize(String name, Expr ... exprs) {
        StringBuilder bob = new StringBuilder();
        
        bob.append("(").append(name);
        for (Expr expr : exprs) {
            bob.append(" ");
            bob.append(expr.accept(this));
        }
        bob.append(")");
        
        return bob.toString();
    }
}
