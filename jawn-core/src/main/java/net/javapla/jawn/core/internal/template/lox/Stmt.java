package net.javapla.jawn.core.internal.template.lox;

abstract class Stmt {
    
    interface Visitor<R> {
        R visitExpression(Expression stmt);
        R visitPrint(Print stmt);
        R visitVar(Var stmt);
    }

    abstract <R> R accept(Visitor<R> visitor);
    
    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpression(this);
        }
    }
    
    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrint(this);
        }
    }
    
    static class Var extends Stmt {
        final Token name;
        final Expr initialiser;

        Var(Token name, Expr initialiser) {
            this.name = name;
            this.initialiser = initialiser;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVar(this);
        }
    }
}
