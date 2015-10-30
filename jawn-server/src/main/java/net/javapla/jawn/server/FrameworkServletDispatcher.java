package net.javapla.jawn.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javapla.jawn.core.FrameworkEngine;
import net.javapla.jawn.core.http.Context;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Simple servlet that allows to run the framework inside a servlet container
 * 
 * *Not fully implemented*
 * 
 * @author MTD
 */
public class FrameworkServletDispatcher extends HttpServlet {

    private static final long serialVersionUID = -7219362539542656401L;

    Injector injector;
    FrameworkEngine framework;
    
    
    @Inject
    public FrameworkServletDispatcher(Injector injector, FrameworkEngine framework) {
        this.injector = injector;
        this.framework = framework;
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        ServletContext servletContext = getServletContext();
        
        ContextImpl context = (ContextImpl) injector.getInstance(Context.class);
        context.init(/*servletContext, */req, resp);
        
        framework.runRequest(context);
    }
    
    @Override
    public void destroy() {
        framework.onFrameworkShutdown();
    }
}
