package org.stringtemplate.v4;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;

import org.stringtemplate.v4.debug.EvalTemplateEvent;
import org.stringtemplate.v4.debug.InterpEvent;
import org.stringtemplate.v4.gui.STViz;
import org.stringtemplate.v4.misc.ErrorBuffer;
import org.stringtemplate.v4.misc.ErrorManager;

public class CustomST extends ST {
    
    public CustomST() {
        super();
    }
    
    public CustomST(ST proto) {
        super(proto);
    }

    @Override
    public int write(STWriter out) throws IOException {
        Interpreter interp = new ExtendedInterpreter(groupThatCreatedThisInstance,
                                             impl.nativeGroup.errMgr,
                                             false);
        InstanceScope scope = new InstanceScope(null, this);
        return interp.exec(out, scope);
    }

    @Override
    public int write(STWriter out, Locale locale) {
        Interpreter interp = new ExtendedInterpreter(groupThatCreatedThisInstance,
                                             locale,
                                             impl.nativeGroup.errMgr,
                                             false);
        InstanceScope scope = new InstanceScope(null, this);
        return interp.exec(out, scope);
    }

    @Override
    public int write(STWriter out, STErrorListener listener) {
        Interpreter interp = new ExtendedInterpreter(groupThatCreatedThisInstance,
                                             new ErrorManager(listener),
                                             false);
        InstanceScope scope = new InstanceScope(null, this);
        return interp.exec(out, scope);
    }

    @Override
    public int write(STWriter out, Locale locale, STErrorListener listener) {
        Interpreter interp = new ExtendedInterpreter(groupThatCreatedThisInstance,
                                             locale,
                                             new ErrorManager(listener),
                                             false);
        InstanceScope scope = new InstanceScope(null, this);
        return interp.exec(out, scope);
    }
    
    @Override
    public STViz inspect(ErrorManager errMgr, Locale locale, int lineWidth) {
        ErrorBuffer errors = new ErrorBuffer();
        impl.nativeGroup.setListener(errors);
        StringWriter out = new StringWriter();
        STWriter wr = new AutoIndentWriter(out);
        wr.setLineWidth(lineWidth);
        Interpreter interp =
            new ExtendedInterpreter(groupThatCreatedThisInstance, locale, true);
        InstanceScope scope = new InstanceScope(null, this);
        interp.exec(wr, scope); // render and track events
        List<InterpEvent> events = interp.getEvents();
        EvalTemplateEvent overallTemplateEval =
            (EvalTemplateEvent)events.get(events.size()-1);
        STViz viz = new STViz(errMgr, overallTemplateEval, out.toString(), interp,
                              interp.getExecutionTrace(), errors.errors);
        viz.open();
        return viz;
    }
    
    @Override
    public List<InterpEvent> getEvents(Locale locale, int lineWidth) {
        StringWriter out = new StringWriter();
        STWriter wr = new AutoIndentWriter(out);
        wr.setLineWidth(lineWidth);
        Interpreter interp =
            new ExtendedInterpreter(groupThatCreatedThisInstance, locale, true);
        InstanceScope scope = new InstanceScope(null, this);
        interp.exec(wr, scope); // render and track events
        return interp.getEvents();
    }
}
