package net.javapla.jawn.template.stringtemplate.rewrite;

import java.nio.file.NoSuchFileException;

import org.stringtemplate.v4.InstanceScope;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.compiler.CompiledST;
import org.stringtemplate.v4.compiler.Compiler;
import org.stringtemplate.v4.compiler.FormalArgument;
import org.stringtemplate.v4.misc.ErrorType;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import net.javapla.jawn.template.stringtemplate.ViewTemplateLoader;


public class FastSTGroup extends STGroup {
    
    private final ViewTemplateLoader templateLoader;

    public FastSTGroup(ViewTemplateLoader loader, char delimiterStart, char delimiterEnd) {
        super(delimiterStart, delimiterEnd);
        
        this.templateLoader = loader;
        
        STGroupDir.verbose = false;
        Interpreter.trace = false;
        
        STGroupDir.trackCreationEvents = true;
        
        // overwrite the ObjectModelAdaptor
        adaptors.put(Object.class, new ObjectWithAttributeNamedGettersModelAdaptor());
        adaptors.put(Object[].class, new ModelAdaptor<Object>() {
            @Override
            public Object getProperty(
                                      Interpreter interp,
                                      ST self,
                                      Object model,
                                      Object property,
                                      String propertyName) throws STNoSuchPropertyException {
                return ((Object[])model)[Integer.valueOf(propertyName)];
            }
        });
        
        FormalArgument argument = new FormalArgument("link");
        argument.index = 0;
        loadTemplate("/style", "smaddermanden $link$").addArg(argument);
        System.out.println(templates);
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
        System.out.println("lookupTemplate("+name+","+template+")");
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
        //templates.put(fullyQualifiedTemplateName, impl); // already done in #rawDefineTemplate
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
        System.out.println("getEmbeddedInstanceOf " + fullyQualifiedName + " " + code + " " + templates);
        if ( code == null) {
            FormalArgument argument = new FormalArgument("link");
            argument.index = 0;
            code = loadTemplate("/style", "smaddermanden $link$");
            code.addArg(argument);
        }
        
        
        // TODO not fully tested and does clearly not handle if templateLoader returns an invalid response
        if (code == null) { // we might need to look at the filesystem
            try {
                code = loadTemplate(fullyQualifiedName, templateLoader.loadTemplate(fullyQualifiedName + TEMPLATE_FILE_EXTENSION));
            } catch (NoSuchFileException e) {
                code = NOT_FOUND_ST;
            }
        }
        
        
        if ( code == NOT_FOUND_ST ) {
            errMgr.runTimeError(interp, scope, ErrorType.NO_SUCH_TEMPLATE, fullyQualifiedName);
            return createStringTemplateInternally(new CompiledST());
        }
        
        return createStringTemplate(code); //tryClone() ?
    }

}
