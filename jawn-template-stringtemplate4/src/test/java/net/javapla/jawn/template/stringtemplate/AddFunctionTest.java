package net.javapla.jawn.template.stringtemplate;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.NoSuchFileException;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.stringtemplate.v4.FastSTGroup;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.compiler.Bytecode;
import org.stringtemplate.v4.compiler.Compiler;
import org.stringtemplate.v4.compiler.Bytecode.Instruction;
import org.stringtemplate.v4.compiler.Bytecode.OperandType;

class AddFunctionTest {

    @Test
    void test() throws NoSuchFileException {
        
        
        
        HashMap<String, Short> newFuncs = new HashMap<>();
        newFuncs.putAll(Compiler.funcs);
        newFuncs.put("style", (short)49);
        Compiler.funcs = newFuncs;
        
        Instruction[] instructions = new Instruction[Bytecode.instructions.length +1];
        System.arraycopy(Bytecode.instructions, 0, instructions, 0, Bytecode.instructions.length);
        instructions[instructions.length -1] = new Instruction("style", OperandType.STRING);
        Bytecode.instructions = instructions;
        
        
        DeploymentInfo info = new DeploymentInfo();
        ViewTemplateLoader loader = new ViewTemplateLoader(info);
        FastSTGroup group = new FastSTGroup(loader, '$', '$');
        group.defineTemplate("functionality", "<html><head>$style(\"stylesheetfile\")$</head></html>");
        ST st = group.getInstanceOf("functionality");
        //System.out.println(st.render());
        
        group.defineTemplate("func2", "<html><head>$css(\"stylesheetfile\")$</head></html>");
        st = group.getInstanceOf("func2");
        System.out.println(st.render());
        //st = group.getInstanceOf("/functions/inject_function", loader.loadTemplate("/functions/inject_function.st"));
        //System.out.println(st.render());
    }

}
