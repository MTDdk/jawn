package net.javapla.jawn.core.internal.template.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();
    
    final Environment enclosing;
    
    Environment() {
        this(null);
    }
    
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
    
    void define(String name, Object value) {
        values.put(name, value);
    }
    
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        
        if (enclosing != null) return enclosing.get(name);
        
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
    
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
