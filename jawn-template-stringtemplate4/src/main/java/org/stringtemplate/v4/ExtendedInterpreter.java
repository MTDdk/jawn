package org.stringtemplate.v4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.stringtemplate.v4.Interpreter.ArgumentsMap;
import org.stringtemplate.v4.Interpreter.ObjectList;
import org.stringtemplate.v4.compiler.Bytecode;
import org.stringtemplate.v4.compiler.Compiler;
import org.stringtemplate.v4.debug.EvalTemplateEvent;
import org.stringtemplate.v4.misc.ErrorManager;
import org.stringtemplate.v4.misc.ErrorType;
import org.stringtemplate.v4.misc.Misc;
import org.stringtemplate.v4.misc.STNoSuchAttributeException;

public class ExtendedInterpreter extends Interpreter {
    
    public ExtendedInterpreter(STGroup group, Locale locale, boolean debug) {
        super(group, locale, debug);
    }
    
    public ExtendedInterpreter(STGroup group, ErrorManager errMgr, boolean debug) {
        super(group, errMgr, debug);
    }

    public ExtendedInterpreter(STGroup group, Locale locale, ErrorManager errMgr, boolean debug) {
        super(group, locale, errMgr, debug);
    }

    @Override
    protected int _exec(STWriter out, InstanceScope scope) {
        final ST self = scope.st;
        int start = out.index(); // track char we're about to write
        int prevOpcode = 0;
        int n = 0; // how many char we write out
        int nargs;
        int nameIndex;
        int addr;
        String name;
        Object o, left, right;
        ST st;
        Object[] options;
        byte[] code = self.impl.instrs;        // which code block are we executing
        int ip = 0;
        while ( ip < self.impl.codeSize ) {
            if ( trace || debug ) trace(scope, ip);
            short opcode = code[ip];
            //count[opcode]++;
            scope.ip = ip;
            ip++; //jump to next instruction or first byte of operand
            switch (opcode) {
                case Bytecode.INSTR_LOAD_STR :
                    // just testing...
                    load_str(self,ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    break;
                case Bytecode.INSTR_LOAD_ATTR :
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.impl.strings[nameIndex];
                    try {
                        o = getAttribute(scope, name);
                        if ( o==ST.EMPTY_ATTR ) o = null;
                    }
                    catch (STNoSuchAttributeException nsae) {
                        errMgr.runTimeError(this, scope, ErrorType.NO_SUCH_ATTRIBUTE, name);
                        o = null;
                    }
                    operands[++sp] = o;
                    break;
                case Bytecode.INSTR_LOAD_LOCAL:
                    int valueIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = self.locals[valueIndex];
                    if ( o==ST.EMPTY_ATTR ) o = null;
                    operands[++sp] = o;
                    break;
                case Bytecode.INSTR_LOAD_PROP :
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[sp--];
                    name = self.impl.strings[nameIndex];
                    operands[++sp] = getObjectProperty(out, scope, o, name);
                    break;
                case Bytecode.INSTR_LOAD_PROP_IND :
                    Object propName = operands[sp--];
                    o = operands[sp];
                    operands[sp] = getObjectProperty(out, scope, o, propName);
                    break;
                case Bytecode.INSTR_NEW :
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.impl.strings[nameIndex];
                    nargs = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    // look up in original hierarchy not enclosing template (variable group)
                    // see TestSubtemplates.testEvalSTFromAnotherGroup()
                    st = self.groupThatCreatedThisInstance.getEmbeddedInstanceOf(this, scope, name);
                    // get n args and store into st's attr list
                    storeArgs(scope, nargs, st);
                    sp -= nargs;
                    operands[++sp] = st;
                    break;
                case Bytecode.INSTR_NEW_IND:
                    nargs = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = (String)operands[sp-nargs];
                    st = self.groupThatCreatedThisInstance.getEmbeddedInstanceOf(this, scope, name);
                    storeArgs(scope, nargs, st);
                    sp -= nargs;
                    sp--; // pop template name
                    operands[++sp] = st;
                    break;
                case Bytecode.INSTR_NEW_BOX_ARGS :
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.impl.strings[nameIndex];
                    Map<String, Object> attrs = (ArgumentsMap)operands[sp--];
                    // look up in original hierarchy not enclosing template (variable group)
                    // see TestSubtemplates.testEvalSTFromAnotherGroup()
                    st = self.groupThatCreatedThisInstance.getEmbeddedInstanceOf(this, scope, name);
                    // get n args and store into st's attr list
                    storeArgs(scope, attrs, st);
                    operands[++sp] = st;
                    break;
                case Bytecode.INSTR_SUPER_NEW :
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.impl.strings[nameIndex];
                    nargs = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    super_new(scope, name, nargs);
                    break;
                case Bytecode.INSTR_SUPER_NEW_BOX_ARGS :
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.impl.strings[nameIndex];
                    attrs = (ArgumentsMap)operands[sp--];
                    super_new(scope, name, attrs);
                    break;
                case Bytecode.INSTR_STORE_OPTION:
                    int optionIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[sp--];    // value to store
                    options = (Object[])operands[sp]; // get options
                    options[optionIndex] = o; // store value into options on stack
                    break;
                case Bytecode.INSTR_STORE_ARG:
                    nameIndex = getShort(code, ip);
                    name = self.impl.strings[nameIndex];
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[sp--];
                    attrs = (ArgumentsMap)operands[sp];
                    attrs.put(name, o); // leave attrs on stack
                    break;
                case Bytecode.INSTR_WRITE :
                    o = operands[sp--];
                    int n1 = writeObjectNoOptions(out, scope, o);
                    n += n1;
                    nwline += n1;
                    break;
                case Bytecode.INSTR_WRITE_OPT :
                    options = (Object[])operands[sp--]; // get options
                    o = operands[sp--];                 // get option to write
                    int n2 = writeObjectWithOptions(out, scope, o, options);
                    n += n2;
                    nwline += n2;
                    break;
                case Bytecode.INSTR_MAP :
                    st = (ST)operands[sp--]; // get prototype off stack
                    o = operands[sp--];      // get object to map prototype across
                    map(scope,o,st);
                    break;
                case Bytecode.INSTR_ROT_MAP :
                    int nmaps = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    List<ST> templates = new ArrayList<ST>();
                    for (int i=nmaps-1; i>=0; i--) templates.add((ST)operands[sp-i]);
                    sp -= nmaps;
                    o = operands[sp--];
                    if ( o!=null ) rot_map(scope,o,templates);
                    break;
                case Bytecode.INSTR_ZIP_MAP:
                    st = (ST)operands[sp--];
                    nmaps = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    List<Object> exprs = new ObjectList();
                    for (int i=nmaps-1; i>=0; i--) exprs.add(operands[sp-i]);
                    sp -= nmaps;
                    operands[++sp] = zip_map(scope, exprs, st);
                    break;
                case Bytecode.INSTR_BR :
                    ip = getShort(code, ip);
                    break;
                case Bytecode.INSTR_BRF :
                    addr = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = operands[sp--]; // <if(expr)>...<endif>
                    if ( !testAttributeTrue(o) ) ip = addr; // jump
                    break;
                case Bytecode.INSTR_OPTIONS :
                    operands[++sp] = new Object[Compiler.NUM_OPTIONS];
                    break;
                case Bytecode.INSTR_ARGS:
                    operands[++sp] = new ArgumentsMap();
                    break;
                case Bytecode.INSTR_PASSTHRU :
                    nameIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    name = self.impl.strings[nameIndex];
                    attrs = (ArgumentsMap)operands[sp];
                    passthru(scope, name, attrs);
                    break;
                case Bytecode.INSTR_LIST :
                    operands[++sp] = new ObjectList();
                    break;
                case Bytecode.INSTR_ADD :
                    o = operands[sp--];             // pop value
                    List<Object> list = (ObjectList)operands[sp]; // don't pop list
                    addToList(scope, list, o);
                    break;
                case Bytecode.INSTR_TOSTR :
                    // replace with string value; early eval
                    operands[sp] = toString(out, scope, operands[sp]);
                    break;
                case Bytecode.INSTR_FIRST  :
                    operands[sp] = first(scope, operands[sp]);
                    break;
                case Bytecode.INSTR_LAST   :
                    operands[sp] = last(scope, operands[sp]);
                    break;
                case Bytecode.INSTR_REST   :
                    operands[sp] = rest(scope, operands[sp]);
                    break;
                case Bytecode.INSTR_TRUNC  :
                    operands[sp] = trunc(scope, operands[sp]);
                    break;
                case Bytecode.INSTR_STRIP  :
                    operands[sp] = strip(scope, operands[sp]);
                    break;
                case Bytecode.INSTR_TRIM   :
                    o = operands[sp--];
                    if ( o.getClass() == String.class ) {
                        operands[++sp] = ((String)o).trim();
                    }
                    else {
                        errMgr.runTimeError(this, scope, ErrorType.EXPECTING_STRING, "trim", o.getClass().getName());
                        operands[++sp] = o;
                    }
                    break;
                case Bytecode.INSTR_LENGTH :
                    operands[sp] = length(operands[sp]);
                    break;
                case Bytecode.INSTR_STRLEN :
                    o = operands[sp--];
                    if ( o.getClass() == String.class ) {
                        operands[++sp] = ((String)o).length();
                    }
                    else {
                        errMgr.runTimeError(this, scope, ErrorType.EXPECTING_STRING, "strlen", o.getClass().getName());
                        operands[++sp] = 0;
                    }
                    break;
                case Bytecode.INSTR_REVERSE :
                    operands[sp] = reverse(scope, operands[sp]);
                    break;
                case Bytecode.INSTR_NOT :
                    operands[sp] = !testAttributeTrue(operands[sp]);
                    break;
                case Bytecode.INSTR_OR :
                    right = operands[sp--];
                    left = operands[sp--];
                    operands[++sp] = testAttributeTrue(left) || testAttributeTrue(right);
                    break;
                case Bytecode.INSTR_AND :
                    right = operands[sp--];
                    left = operands[sp--];
                    operands[++sp] = testAttributeTrue(left) && testAttributeTrue(right);
                    break;
                case Bytecode.INSTR_INDENT :
                    int strIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    indent(out, scope, strIndex);
                    break;
                case Bytecode.INSTR_DEDENT :
                    out.popIndentation();
                    break;
                case Bytecode.INSTR_NEWLINE :
                    try {
                        if ( (prevOpcode==0 && !self.isAnonSubtemplate() && !self.impl.isRegion) ||
                            prevOpcode==Bytecode.INSTR_NEWLINE ||
                            prevOpcode==Bytecode.INSTR_INDENT ||
                            nwline>0 )
                        {
                            out.write(Misc.newline);
                        }
                        nwline = 0;
                    }
                    catch (IOException ioe) {
                        errMgr.IOError(self, ErrorType.WRITE_IO_ERROR, ioe);
                    }
                    break;
                case Bytecode.INSTR_NOOP :
                    break;
                case Bytecode.INSTR_POP :
                    sp--; // throw away top of stack
                    break;
                case Bytecode.INSTR_NULL :
                    operands[++sp] = null;
                    break;
                case Bytecode.INSTR_TRUE :
                    operands[++sp] = true;
                    break;
                case Bytecode.INSTR_FALSE :
                    operands[++sp] = false;
                    break;
                case Bytecode.INSTR_WRITE_STR :
                    strIndex = getShort(code, ip);
                    ip += Bytecode.OPND_SIZE_IN_BYTES;
                    o = self.impl.strings[strIndex];
                    n1 = writeObjectNoOptions(out, scope, o);
                    n += n1;
                    nwline += n1;
                    break;
                // TODO: generate this optimization
//              case Bytecode.INSTR_WRITE_LOCAL:
//                  valueIndex = getShort(code, ip);
//                  ip += Bytecode.OPND_SIZE_IN_BYTES;
//                  o = self.locals[valueIndex];
//                  if ( o==ST.EMPTY_ATTR ) o = null;
//                  n1 = writeObjectNoOptions(out, self, o);
//                  n += n1;
//                  nwline += n1;
//                  break;
                case 49: 
                    
                    operands[sp] = stylesheet(scope, operands[sp]);
                    break;
                default :
                    errMgr.internalError(self, "invalid bytecode @ "+(ip-1)+": "+opcode, null);
                    self.impl.dump();
            }
            prevOpcode = opcode;
        }
        if ( debug ) {
            int stop = out.index() - 1;
            EvalTemplateEvent e = new EvalTemplateEvent(scope, start, stop);
            trackDebugEvent(scope, e);
        }
        return n;
    }
    
    public Object stylesheet(InstanceScope scope, Object v) {
        /*if ( v==null ) return null;
        v = convertAnythingIteratableToIterator(scope, v);
        if ( v instanceof Iterator ) {
            List<Object> a = new LinkedList<Object>();
            Iterator<?> it = (Iterator<?>)v;
            while (it.hasNext()) a.add(0, it.next());
            return a;
        }
        return v;*/
        return "<import stylesheet="+v+" />";
    }
}
