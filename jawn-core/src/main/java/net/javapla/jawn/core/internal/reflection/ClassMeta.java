package net.javapla.jawn.core.internal.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import net.javapla.jawn.core.annotation.Inject;
import net.javapla.jawn.core.annotation.Singleton;

@Singleton
public class ClassMeta {
    
    private static final String[] NO_ARG = {};
    
    //private final Map<Class<?>, Map<Executable, String[]>> metadataCache; // TODO is this a "good" thing? Can it be converted into being used on a controller basis
    private final ClassSource classSource;

    @Inject
    public ClassMeta(ClassSource classSource) {
        //this.metadataCache = new ConcurrentHashMap<>();
        this.classSource = classSource;
    }

    public String[] parameterNames(Executable exe) {
        return extractParameterNames(exe.getDeclaringClass()).get(exe);
    }

    public Map<Executable, String[]> extractParameterNames(Class<?> owner) {
        //return metadataCache.computeIfAbsent(owner, c -> {
            ClassMethodsVisitor visitor = new ClassMethodsVisitor(owner);
            
            new ClassReader(classSource.byteCode(owner)).accept(visitor, 0);
            classSource.reload(owner); // clean up after yourself - enforces the framework to reload the class from disk if revisited
            
            return visitor.parameterNames;
        //});
    }
    
    private static class ClassMethodsVisitor extends ClassVisitor {
        
        final Class<?> c;
        final Map<Executable, String[]> parameterNames;
        final Map<String, Executable> methods;
        final boolean debug;
        
        protected ClassMethodsVisitor(Class<?> c, boolean debug) {
            super(Opcodes.ASM9);
            this.c = c;
            this.methods = new HashMap<>();
            this.parameterNames = new HashMap<>();
            this.debug = debug;
            readMethodDescriptors();
        }

        protected ClassMethodsVisitor(Class<?> c) {
            this(c, false);
        }
        
        private void readMethodDescriptors() {
            for (Method method : c.getMethods()) {
                String descriptor = Type.getMethodDescriptor(method);
                //System.out.println(method.getName() + descriptor);
                methods.put(method.getName() + descriptor, method);
            }
            
            for (Constructor<?> constructor : c.getConstructors()) {
                String descriptor = Type.getConstructorDescriptor(constructor);
                methods.put("<init>" + descriptor, constructor);
            }
            
            // .getDeclaredMethods vs .getMethods ->
            // the former returns ONLY declared in this particular class,
            // the latter returns ALL in the hierarchy
            // - and we probably want that in this class
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            //MethodNode node = new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions);
            
            Executable method = methods.get(name + descriptor);
            if (method == null) return null;
            
            final int arguments = Type.getArgumentTypes(descriptor).length;
            if (arguments == 0) {
                parameterNames.put(method, NO_ARG);
                return null;
            }
            
            if (debug) System.out.println(method);
            
            final String[] names = new String[arguments];
            parameterNames.put(method, names);
            
            return new MethodVisitor(Opcodes.ASM9) {
                int index = 0;
                
                /*@Override
                public void visitParameter(String name, int access) {
                // can only be used if compiled with "-parameters"
                    names[index++] = name;
                    System.out.println(name);
                }*/
                
                @Override
                public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int i) {
                    
                    if (i == 0 || i > arguments) {
                        // current variable is either "this" or some other variable within the method scope not part of the parameter list
                        return;
                    }
                    
                    if (debug) System.out.println("--  " + name + "  " + descriptor + "  " + signature + "  " + i);
                    
                    names[index++] = name;
                    
                }
                @Override
                public void visitEnd() {
                    if (debug) System.out.println();
                }
            };
        }
        
    }
}
