package net.javapla.jawn.core.internal.template.lox;

import java.util.List;

import net.javapla.jawn.core.internal.template.lox.Expr.Assign;
import net.javapla.jawn.core.internal.template.lox.Expr.Binary;
import net.javapla.jawn.core.internal.template.lox.Expr.Grouping;
import net.javapla.jawn.core.internal.template.lox.Expr.Literal;
import net.javapla.jawn.core.internal.template.lox.Expr.This;
import net.javapla.jawn.core.internal.template.lox.Expr.Unary;
import net.javapla.jawn.core.internal.template.lox.Expr.Variable;
import net.javapla.jawn.core.internal.template.lox.Stmt.Expression;
import net.javapla.jawn.core.internal.template.lox.Stmt.Print;
import net.javapla.jawn.core.internal.template.lox.Stmt.Var;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    
    private Environment environment = new Environment();
    
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    @Override
    public Object visitAssign(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinary(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        
        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right; 
                }
                
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                
                break;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
                
            default: break;
        }
        return null;
    }

    @Override
    public Object visitGrouping(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteral(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitThis(This expr) {
        return null;
    }

    @Override
    public Object visitUnary(Unary expr) {
        Object right = evaluate(expr.right);
        
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            default:
                return null;
        }
    }
    
    @Override
    public Object visitVariable(Variable expr) {
        return environment.get(expr.name);
    }
    
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
    
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be a numbers.");
    }
    
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (boolean)o;
        return true;
    }
    
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
        // return Objects.equals(a, b);
    }
    
    private String stringify(Object o) {
        if (o == null) return "nil";
        
        if (o instanceof Double) {
            String text = o.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        
        return o.toString();
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
    
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }
    
    @Override
    public Void visitExpression(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }
    
    @Override
    public Void visitPrint(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }
    
    @Override
    public Void visitVar(Var stmt) {
        Object value = null;
        if (stmt.initialiser != null) {
            value = evaluate(stmt.initialiser);
        }
        
        environment.define(stmt.name.lexeme, value);
        return null;
    }
}
