package net.javapla.jawn.template.stringtemplate;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
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
        
        Instruction[] instructions = new Instruction[Bytecode.instructions.length +2];
        System.arraycopy(Bytecode.instructions, 0, instructions, 0, Bytecode.instructions.length);
        instructions[instructions.length -2] = new Instruction("css");//, OperandType.STRING);
        instructions[instructions.length -1] = new Instruction("coolios", OperandType.ADDR);
        Bytecode.instructions = instructions;

        HashMap<String, Short> newFuncs = new HashMap<>();
        newFuncs.putAll(Compiler.funcs);
        newFuncs.put("css", (short)49);
        newFuncs.put("coolios", (short)50);
        Compiler.funcs = newFuncs;
        
        
        
        DeploymentInfo info = new DeploymentInfo();
        ViewTemplateLoader loader = new ViewTemplateLoader(info);
        FastSTGroup group = new FastSTGroup(loader, '$', '$');
        //group.defineTemplate("functionality", "<html><head>$style(\"stylesheetfile\")$</head></html>");
        //ST st = group.getInstanceOf("functionality");
        //System.out.println(st.render());
        
        group.defineTemplate("/func2", "<html><head>$coolios()$</head><body><main>$css(\"stylesheetfile2\")$</main></body></html>");
        ST st = group.getInstanceOf("/func2");
        System.out.println(st.render());
        //st = group.getInstanceOf("/functions/inject_function", loader.loadTemplate("/functions/inject_function.st"));
        //System.out.println(st.render());
    }

}
