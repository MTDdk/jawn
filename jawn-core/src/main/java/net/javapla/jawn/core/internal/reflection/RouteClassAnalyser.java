package net.javapla.jawn.core.internal.reflection;

import static java.util.Optional.ofNullable;
import static java.util.Spliterator.ORDERED;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import net.javapla.jawn.core.ReifiedGenerics;
import net.javapla.jawn.core.Up;

public class RouteClassAnalyser {
    
    private final ClassSource source;
    private final boolean debug;
    private final TypeParser typeParser;

    public RouteClassAnalyser(ClassSource source) {
        this(source, false);
    }
    
    RouteClassAnalyser(ClassSource source, boolean debug) {
        this.source = source;
        this.debug = debug;
        this.typeParser = new TypeParser(source.loader);
    }

    
    public Type returnType(Object handler) {
        try {
            
            Method method = methodOf(handler);
            if (method == null) {
                return Object.class;
            }
            
            Class<?> returnType = method.getReturnType();
            if (returnType != Object.class) {
                return method.getGenericReturnType();
            }
            
            // We will need to read the bytecode directly from the class file
            ClassReader reader = new ClassReader(source.byteCode(method.getDeclaringClass()));
            RouteClassMethodVisitor visitor = new RouteClassMethodVisitor(method, debug);
            reader.accept(visitor, 0);
            
            if (debug) {
                
            }
            
            return visitor.find(typeParser);
            
        } catch (Exception e) {
            throw Up.ParseError("", e);
        }
        
    }
    
    private Method methodOf(Object handler) throws Exception {
        Method method = Reflection.getLambdaMethod(source.loader, handler);
        
        // Do some checks in case the handler is a method call of some sort
        /*if (method == null) {
            Method[] methods = handler.getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (m.getReturnType() == Object.class) {
                    method = m;
                }
            }
        }*/
        
        return method;
    }
    
    private static final class RouteClassMethodVisitor extends ClassVisitor {
        
        final String _descriptor;
        final String _name;
        final boolean debug;
        
        MethodNode node;
        ASMifier printer;
        
        RouteClassMethodVisitor(final Method method, final boolean debug) {
            super(Opcodes.ASM9);
            this.debug = debug;
            _descriptor = org.objectweb.asm.Type.getMethodDescriptor(method);
            _name = method.getName();
        }
            
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

            MethodVisitor sup = super.visitMethod(access, name, descriptor, signature, exceptions);

            if (_name.equals(name) && _descriptor.equals(descriptor)) {
                node = new MethodNode(access, name, descriptor, signature, exceptions);
                if (debug) {
                    if (printer == null) {
                        printer = new ASMifier();
                    }
                    return new TraceMethodVisitor(this.node, printer);
                }
                return node;
            }

            return sup;
        }
        
        
        Type find(final TypeParser parser) {
            Set<java.lang.reflect.Type> types = new LinkedHashSet<>();
            
            List<AbstractInsnNode> returns = findReturns(this.node);
            
            for (AbstractInsnNode node : returns) {
                AbstractInsnNode previous = previous(node.getPrevious());
                
                if (previous instanceof MethodInsnNode) {
                    MethodInsnNode m = ((MethodInsnNode) previous);
                    // Constructor
                    if (m.name.equals("<init>")) {
                        types.add(parser.resolve(m.owner));
                    } else {
                        types.add(parser.typeDescriptor(org.objectweb.asm.Type.getReturnType(m.desc).getDescriptor()));
                    }
                } else if (previous instanceof VarInsnNode) {
                    Type type = localVariable(parser, this.node, (VarInsnNode) previous);
                    if (type != null) types.add(type);
                } else if (previous instanceof InvokeDynamicInsnNode) {
                    // visitInvokeDynamicInsn("call", "()Ljava/util/concurrent/Callable;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), new Object[]{Type.getType("()Ljava/lang/Object;"), new Handle(Opcodes.H_INVOKESTATIC, "io/jooby/internal/ReturnTypeTest", "lambda$null$11", "()Ljava/lang/Character;", false), Type.getType("()Ljava/lang/Character;")});
                    // (Callable<Character>) () -> 'x'
                    InvokeDynamicInsnNode invokeDynamic = (InvokeDynamicInsnNode) previous;
                    String handleDescriptor = Stream.of(invokeDynamic.bsmArgs)
                        .filter(Handle.class::isInstance)
                        .map(Handle.class::cast)
                        .findFirst()
                        .map(h -> {
                            String desc = org.objectweb.asm.Type.getReturnType(h.getDesc()).getDescriptor();
                            return "V".equals(desc) ? "java/lang/Object" : desc;
                        })
                        .orElse(null);
                    // Ljava/util/concurrent/Callable;
                    String descriptor = org.objectweb.asm.Type
                        .getReturnType(invokeDynamic.desc)
                        .getDescriptor();
                    if (handleDescriptor != null) {
                        // Handle: ()Ljava/lang/Character;
                        if (descriptor.endsWith(";")) {
                            descriptor = descriptor.substring(0, descriptor.length() - 1);
                        }
                        descriptor += "<" + handleDescriptor + ">";
                    }
                    types.add(parser.typeDescriptor(descriptor));
                } else if (previous instanceof LdcInsnNode) {
                    /** return "String" | int | double */
                    Object cst = ((LdcInsnNode) previous).cst;
                    if (cst instanceof Type) {
                        types.add(parser.typeDescriptor(((org.objectweb.asm.Type) cst).getDescriptor()));
                    } else {
                        types.add(cst.getClass());
                    }
                } else {
                    // array
                    switch (previous.getOpcode()) {
                        case Opcodes.NEWARRAY -> {
                            Class<?> arr = primitiveEmptyArray(previous);
                            if (arr != null) types.add(arr);
                        }
                        case Opcodes.ANEWARRAY -> {
                            TypeInsnNode typeInsn = (TypeInsnNode) previous;
                            types.add(parser.typeDescriptor("[" + typeInsn.desc));
                        }
                        case Opcodes.BASTORE -> {
                            types.add(boolean[].class);
                        }
                        case Opcodes.CASTORE -> {
                            types.add(char[].class);
                        }
                        case Opcodes.SASTORE -> {
                            types.add(short[].class);
                        }
                        case Opcodes.IASTORE -> {
                            types.add(int[].class);
                        }
                        case Opcodes.LASTORE -> {
                            types.add(long[].class);
                        }
                        case Opcodes.FASTORE -> {
                            types.add(float[].class);
                        }
                        case Opcodes.DASTORE -> {
                            types.add(double[].class);
                        }
                        case Opcodes.AASTORE -> {
                            Stream<AbstractInsnNode> stream = 
                                StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<AbstractInsnNode>() {

                                    AbstractInsnNode node = previous;
                                    
                                    @Override
                                    public boolean hasNext() {
                                        return node != null;
                                    }

                                    @Override
                                    public AbstractInsnNode next() {
                                        AbstractInsnNode current = node;
                                        node = node.getPrevious();
                                        return current;
                                    }
                                    
                                }, ORDERED), false);
                            
                            return stream
                                .filter(e -> e.getOpcode() == Opcodes.ANEWARRAY)
                                .findFirst()
                                .map(e -> {
                                    TypeInsnNode t = (TypeInsnNode) e;
                                    return parser.typeDescriptor("[" + t.desc);
                                })
                                .orElse(Object.class);
                        }
                    }
                }
            }
            
            
            return types.isEmpty() ? Object.class : parser.commonAncestor(types);
        }
        
        public static List<AbstractInsnNode> findReturns(MethodNode node) {
            List<AbstractInsnNode> result = new ArrayList<>();
            for (AbstractInsnNode instruction : node.instructions) {
                if (instruction.getOpcode() == Opcodes.ARETURN) {
                    result.add(instruction);
                }
            }
            return result;
        }
        
        private static AbstractInsnNode previous(AbstractInsnNode node) {
            while (node != null) {
                if (ignore(node)) {
                    node = node.getPrevious();
                } else {
                    if (LDCInstruction(node)) {
                        node = node.getPrevious().getPrevious();
                    } else {
                        return node;
                    }
                }
            }
            return null;
        }
        
        private static boolean ignore(AbstractInsnNode node) {
            return LabelNode.class.isInstance(node) || LineNumberNode.class.isInstance(node);
        }
        private static boolean LDCInstruction(AbstractInsnNode node) {
            return node instanceof LdcInsnNode ? node.getPrevious() != null && node.getPrevious().getOpcode() == Opcodes.DUP : false;
        }
        
        private static Type localVariable(TypeParser typeParser, MethodNode m, VarInsnNode varInsn) {
            if (varInsn.getOpcode() == Opcodes.ALOAD) {
                List<LocalVariableNode> vars = m.localVariables;
                LocalVariableNode var = vars.stream()
                    .filter(v -> v.index == varInsn.var)
                    .findFirst()
                    .orElse(null);
                if (var != null) {
                    String signature = ofNullable(var.signature).orElse(var.desc);
                    return typeParser.typeDescriptor(signature);
                }
            }
            return null;
        }

        private static Class<?> primitiveEmptyArray(AbstractInsnNode previous) {
            // empty primitive array
            if (previous instanceof IntInsnNode) {
                switch (((IntInsnNode) previous).operand) {
                    case Opcodes.T_BOOLEAN:
                        return boolean[].class;
                    case Opcodes.T_CHAR:
                        return char[].class;
                    case Opcodes.T_BYTE:
                        return byte[].class;
                    case Opcodes.T_SHORT:
                        return short[].class;
                    case Opcodes.T_INT:
                        return int[].class;
                    case Opcodes.T_LONG:
                        return long[].class;
                    case Opcodes.T_FLOAT:
                        return float[].class;
                    case Opcodes.T_DOUBLE:
                        return double[].class;
                }
            }
            return null;
        }
    }
    
    static class TypeParser {
        private final ClassLoader loader;

        public TypeParser(ClassLoader loader) {
            this.loader = loader;
        }
        
        // Type Parser
        Class<?> resolve(String name) {
            try {
                String classname = name.replace('/', '.');
                switch (classname) {
                    case "boolean":
                        return boolean.class;
                    case "char":
                        return char.class;
                    case "byte":
                        return byte.class;
                    case "short":
                        return short.class;
                    case "int":
                        return int.class;
                    case "long":
                        return long.class;
                    case "float":
                        return float.class;
                    case "double":
                        return double.class;
                    case "java.lang.String":
                        return String.class;
                    case "java.lang.Object":
                        return Object.class;
                    default:
                        Class<?> result;
                        if (classname.startsWith("[")) {
                            result = Class.forName(classname, false, loader);
                        } else {
                            result = loader.loadClass(classname);
                        }
                        if (List.class.isAssignableFrom(result)) {
                            return List.class;
                        }
                        if (Set.class.isAssignableFrom(result)) {
                            return Set.class;
                        }
                        if (Map.class.isAssignableFrom(result)) {
                            return Map.class;
                        }
                        return result;
                }
            } catch (Exception e) {
                throw Up.ParseError("", e);
            }
        }
        
        Type typeDescriptor(String descriptor) {
            Type type = simpleType(descriptor, 0, false);
            if (type != null) {
                return type;
            }
            StringBuilder name = new StringBuilder();
            LinkedList<LinkedList<Type>> stack = new LinkedList<>();
            stack.addLast(new LinkedList<>());
            int array = 0;
            for (int i = 0; i < descriptor.length(); ) {
                char ch = descriptor.charAt(i);
                if (ch == '[') {
                    array += 1;
                } else if (ch == 'L') {
                    if (name.length() > 0) {
                        name.append(ch);
                    }
                } else if (ch == '<') {
                    newType(stack, name, array);
                    stack.add(new LinkedList<>());
                    array = 0;
                } else if (ch == ';') {
                    if (name.length() > 0) {
                        newType(stack, name, array);
                        array = 0;
                    }
                } else if (ch == '/') {
                    name.append('.');
                } else if (ch == '>') {
                    newParameterized(stack);
                } else {
                    name.append(ch);
                }
                i += 1;
            }
            if (name.length() > 0) {
                newType(stack, name, array);
            }
            while (stack.size() > 1) {
                newParameterized(stack);
            }
            return stack.getFirst().getFirst();
        }
        
        private Class<?> simpleType(String descriptor, int at, boolean array) {
            switch (descriptor.charAt(at)) {
                case 'V':
                    return void.class;
                case 'Z':
                    return array ? boolean[].class : boolean.class;
                case 'C':
                    return array ? char[].class : char.class;
                case 'B':
                    return array ? byte[].class : byte.class;
                case 'S':
                    return array ? short[].class : short.class;
                case 'I':
                    return array ? int[].class : int.class;
                case 'F':
                    return array ? float[].class : float.class;
                case 'J':
                    return array ? long[].class : long.class;
                case 'D':
                    return array ? double[].class : double.class;
                case '[':
                    return simpleType(descriptor, at + 1, true);
            }
            if (descriptor.equals("Ljava/lang/String;")) {
                return String.class;
            }
            if (descriptor.equals("[Ljava/lang/String;")) {
                return String[].class;
            }
            return null;
        }
        
        private void newParameterized(LinkedList<LinkedList<Type>> stack) {
            Type[] types = stack.removeLast().toArray(new Type[0]);
            LinkedList<Type> parent = stack.peekLast();
            if (parent.size() > 0) {
                Type rawType = parent.removeLast();
                Type paramType = ReifiedGenerics.getParameterized(rawType, types).type;
                parent.addLast(paramType);
            } else {
                parent.add(types[0]);
            }
        }
        
        private void newType(LinkedList<LinkedList<Type>> stack, StringBuilder name, int array) {
            Type it;
            if (array == 0) {
                it = resolve(name.toString());
            } else {
                StringBuilder dimension = new StringBuilder();
                IntStream.range(0, array).forEach(x -> dimension.append('['));
                it = resolve(dimension + "L" + name + ";");
            }
            stack.getLast().add(it);
            name.setLength(0);
        }
        
        public Type commonAncestor(Set<Type> types) {
            if (types.size() == 0) {
                return Object.class;
            }
            if (types.size() == 1) {
                return types.iterator().next();
            }
            Set<Class<?>> classes = determineCommonAncestor(types);
            Iterator<Class<?>> iterator = classes.iterator();
            return iterator.hasNext() ? iterator.next() : Object.class;
        }
        
        private Set<Class<?>> determineCommonAncestor(Set<Type> classes) {
            Iterator<Type> it = classes.iterator();
            // begin with set from first hierarchy
            Set<Class<?>> result = new LinkedHashSet<>();
            superclasses(ReifiedGenerics.rawType(it.next()), result);
            // remove non-superclasses of remaining
            while (it.hasNext()) {
                Class<?> c = ReifiedGenerics.rawType(it.next());
                Iterator<Class<?>> resultIt = result.iterator();
                while (resultIt.hasNext()) {
                    Class<?> sup = resultIt.next();
                    if (!sup.isAssignableFrom(c)) {
                        resultIt.remove();
                    }
                }
            }
            return result;
        }
        
        private void superclasses(Class<?> clazz, Set<Class<?>> result) {
            if (clazz != null && clazz != Object.class) {
                if (result.add(clazz)) {
                    for (Class<?> k : clazz.getInterfaces()) {
                        superclasses(k, result);
                    }
                    superclasses(clazz.getSuperclass(), result);
                }
            }
        }
    }
    
}
