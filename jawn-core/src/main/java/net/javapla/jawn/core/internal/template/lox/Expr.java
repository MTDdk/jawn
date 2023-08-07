package net.javapla.jawn.core.internal.template.lox;

abstract class Expr {
    
    interface Visitor<R> {
        R visitAssign(Assign expr);
        R visitBinary(Binary expr);
        R visitGrouping(Grouping expr);
        R visitLiteral(Literal expr);
        R visitThis(This expr);
        R visitUnary(Unary expr);
    }
    
    abstract <R> R accept(Visitor<R> visitor);
    
    static class Assign extends Unary { // changed from "extends Expr"
        
        Assign(Token name, Expr value) {
            super(name, value);
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssign(this);
        }
        
    }
    
    static class Binary extends Expr {
        final Expr expr;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.expr = left;
            this.operator = operator;
            this.right = right;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinary(this);
        }
    }
    
    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGrouping(this);
        }
    }
    
    static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }
    
    static class This extends Expr {
        final Token keyword;

        This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return null;
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr right;
    
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
        
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnary(this);
        }
    }
}
