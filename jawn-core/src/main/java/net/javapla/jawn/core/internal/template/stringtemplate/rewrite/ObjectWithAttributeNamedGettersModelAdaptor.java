package net.javapla.jawn.core.internal.template.stringtemplate.rewrite;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

public class ObjectWithAttributeNamedGettersModelAdaptor extends ObjectModelAdaptor {

    @Override
    public synchronized Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
        if (o == null) {
            throw new NullPointerException("o");
        }

        Class<?> c = o.getClass();

        if ( property==null ) {
            return throwNoSuchProperty(c, propertyName, null);
        }

        Member member = findMember_overridden(c, propertyName);
        if ( member!=null ) {
            try {
                if (member instanceof Method) {
                    return ((Method)member).invoke(o);
                } else if (member instanceof Field) {
                    return ((Field)member).get(o);
                }
            } catch (Exception e) {
                throwNoSuchProperty(c, propertyName, e);
            }
        }

        return throwNoSuchProperty(c, propertyName, null);
    }

    protected Member findMember_overridden(Class<?> clazz, String memberName) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        if (memberName == null) {
            throw new NullPointerException("memberName");
        }

        synchronized (membersCache) {
            Map<String, Member> members = membersCache.get(clazz);
            Member member = null;
            if (members != null) {
                member = members.get(memberName);
                if (member != null) {
                    return member != INVALID_MEMBER ? member : null;
                }
            } else {
                members = new HashMap<String, Member>();
                membersCache.put(clazz, members);
            }

            member = getMemberMethod(clazz, memberName);

            if (member == null) {
                // try for a visible field
                member = tryGetField(clazz, memberName);
            }

            members.put(memberName, member != null ? member : INVALID_MEMBER);
            return member;
        }
    }
    
    protected Member getMemberMethod(final Class<?> clazz, final String memberName) {
        // MTD: look up the exact name as method
        Member member = tryGetMethod(clazz, memberName);
        if (member != null) return member;
        
        // try getXXX and isXXX properties, look up using reflection
        String methodSuffix = Character.toUpperCase(memberName.charAt(0)) +
            memberName.substring(1, memberName.length());
        
        member = tryGetMethod(clazz, "get" + methodSuffix);
        if (member == null) {
            member = tryGetMethod(clazz, "is" + methodSuffix);
            if (member == null) {
                member = tryGetMethod(clazz, "has" + methodSuffix);
            }
        }
        
        return member;
    }
    
}
