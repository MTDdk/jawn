package net.javapla.jawn.templates.stringtemplate.rewrite;

import org.stringtemplate.v4.InstanceScope;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.compiler.CompiledST;
import org.stringtemplate.v4.compiler.Compiler;
import org.stringtemplate.v4.misc.ErrorType;

/**
 * Overriding STGroup for a shred of greater performance, and because the framework handles all finding and loading of templates.
 * 
 * @author MTD
 */
public final class FastSTGroup extends STGroup {
    
    public FastSTGroup(char delimiterStartChar, char delimiterStopChar) {
        super(delimiterStartChar, delimiterStopChar);
        STGroupDir.verbose = false;
        Interpreter.trace = false;
        
        // overwrite the ObjectModelAdaptor
        adaptors.put(Object.class, new ObjectWithAttributeNamedGettersModelAdaptor());
    }
    
    public ST getInstanceOf(String name, String template ) {
        CompiledST c = lookupTemplate( name, template );
        if ( c!=null ) {
            return createStringTemplate(c);
        }
        return null;
    }
    
    // TODO might need some revision
    public CompiledST lookupTemplate(String name, String template) {
        CompiledST code = rawGetTemplate(name);
        if ( code == NOT_FOUND_ST ) {
            return null; //  previously seen as not found
        }
        if (code != null) return code;//tryClone(code);
        
        if ( code == null ) code = loadTemplate(name, template); // load template into a t.st
        if ( code == null ) templates.put(name, NOT_FOUND_ST);
        
        //TODO rewrite in order to not use 'templates', which is synchronized
        // we can probably manage with only synchronising writes and not all reads
        
        return code;//code != null ? tryClone(code) : null;
    }
    
    public CompiledST loadTemplate(String fullyQualifiedTemplateName, String template) {
        CompiledST impl = new Compiler(this).compile(fullyQualifiedTemplateName, template);
        //CommonToken nameT = new CommonToken(STLexer.SEMI); // Seems like a hack, best I could come up with.
        //nameT.setInputStream(constructStringStream(fullyQualifiedTemplateName, file));
        rawDefineTemplate(fullyQualifiedTemplateName, impl, null/*nameT*/);
        impl.defineImplicitlyDefinedTemplates(this);
        //impl.nativeGroup = this;
        //impl.templateDefStartToken = nameT;
        //impl.prefix = getPrefix(fullyQualifiedTemplateName);
        //templates.put(fullyQualifiedTemplateName, impl);
        return impl;
    }

    @Override
    public final ST getInstanceOf(final String name) {
        CompiledST c = lookupTemplate( ( name.charAt(0)!='/' ) ? '/'+name : name);
        if ( c!=null ) {
            return createStringTemplate(c);
        }
        return null;
    }
    
    @Override
    protected ST getEmbeddedInstanceOf(Interpreter interp, 
                                       InstanceScope scope, 
                                       String name) {
        String fullyQualifiedName = name;
        if ( name.charAt(0)!='/' ) {
            fullyQualifiedName = scope.st.impl.prefix + name;
        }
        CompiledST code = rawGetTemplate(fullyQualifiedName);
        if ( code == NOT_FOUND_ST ) {
            errMgr.runTimeError(interp, scope, ErrorType.NO_SUCH_TEMPLATE, fullyQualifiedName);
            return createStringTemplateInternally(new CompiledST());
        }
        
        return createStringTemplate(code); //tryClone() ?
    }
}
