package net.javapla.jawn.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javapla.jawn.core.http.Context;

interface ContextInternal extends Context.Internal {
    void init(/*ServletContext servletContext, */HttpServletRequest request, HttpServletResponse response);
}
