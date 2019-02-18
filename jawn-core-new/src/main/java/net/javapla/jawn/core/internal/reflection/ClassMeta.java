package net.javapla.jawn.core.internal.reflection;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassMeta {
    
    private static final String[] NO_ARG = new String[0];

    
    private final Map<Class<?>, Map<String, String[]>> metadataCache;
    

    public ClassMeta() {
        // TODO Not exactly thread safe..
        this.metadataCache = new HashMap<>();
    }
    
    public String[] parameterNames(final Executable action) {
        Map<String, String[]> metadata = metadataCache.computeIfAbsent(action.getDeclaringClass(), ClassMeta::extractMetadata);
        return metadata.get(paramsKey(action));
    }
    
    private static Map<String, String[]> extractMetadata(final Class<?> owner) {
        InputStream stream = null;
        try {
            Map<String, String[]> md = new HashMap<>();
            stream = owner.getResource(classfile(owner)).openStream();
            
            // ASM
            new ClassReader(stream).accept(visitor(md), 0);
            
            return md;
        } catch (Exception ex) {
            // won't happen, but...
            throw new IllegalStateException("Can't read class: " + owner.getName(), ex);
        } finally {
            if (stream != null) try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String classfile(final Class<?> owner) {
        StringBuilder sb = new StringBuilder();
        Class<?> dc = owner.getDeclaringClass();
        while (dc != null) {
            sb.insert(0, dc.getSimpleName()).append("$");
            dc = dc.getDeclaringClass();
        }
        sb.append(owner.getSimpleName());
        sb.append(".class");
        return sb.toString();
    }
    
    private static String paramsKey(final Executable exec) {
        return /*paramsKey*/(key(exec));
    }

    /*private static String paramsKey(final String key) {
        return key + ".params";
    }*/
    
    /*private static String startAtKey(final Executable exec) {
        return startAtKey(key(exec));
    }

    private static String startAtKey(final String key) {
        return key + ".startAt";
    }*/
    
    @SuppressWarnings("rawtypes")
    private static String key(final Executable exec) {
        if (exec instanceof Method) {
            return exec.getName() + Type.getMethodDescriptor((Method) exec);
        } else {
            return "<init>" + Type.getConstructorDescriptor((Constructor) exec);
        }
    }

    private static ClassVisitor visitor(final Map<String, String[]> md) {
        return new ClassVisitor(Opcodes.ASM7) {

            @Override
            public MethodVisitor visitMethod(final int access, final String name,
                                             final String desc, final String signature, final String[] exceptions) {
                
                boolean isPublic = ((access & Opcodes.ACC_PUBLIC) > 0) ? true : false;
                boolean isStatic = ((access & Opcodes.ACC_STATIC) > 0) ? true : false;
                
                if (!isPublic || isStatic) {
                    // ignore
                    return null;
                }
                final String seed = name + desc;
                Type[] args = Type.getArgumentTypes(desc);
                
                
                String[] names = args.length == 0 ? NO_ARG : new String[args.length];
                md.put(/*paramsKey*/(seed), names);

                int minIdx = 1/*((access & Opcodes.ACC_STATIC) > 0) ? 0 : 1*/;
                int maxIdx = Arrays.stream(args).mapToInt(Type::getSize).sum();

                return new MethodVisitor(Opcodes.ASM7) {

                    private int i = 0;

                    //private boolean skipLocalTable = false;

                    /*@Override
                    public void visitParameter(final String name, final int access) {
                        skipLocalTable = true;
                        // save current parameter
                        names[i] = name;
                        // move to next
                        i += 1;
                    }*/

                    /*@Override
                    public void visitLineNumber(final int line, final Label start) {
                        // save line number
                        md.putIfAbsent(startAtKey(seed), line);
                    }*/

                    @Override
                    public void visitLocalVariable(final String name, final String desc,
                                                   final String signature,
                                                   final Label start, final Label end, final int index) {
                        //if (!skipLocalTable) {
                            if (index >= minIdx && index <= maxIdx) {
                                // save current parameter
                                names[i] = name;
                                // move to next
                                i += 1;
                            }
                        //}
                    }

                };
            }

        };
    }
}
