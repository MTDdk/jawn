/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package net.javapla.jawn.templatemanagers;

import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * @author Igor Polevoy
 * @author MTD
 */
public interface TemplateManager {

    /**
     * Merges values with templates and writes a merged template to the writer.
     *
     * @param values values to be merged.
     * @param template name of template in format: <code>dir/template</code> without
     * file extension. This is to support multiple template technologies in the future.
     * @param writer Writer to write results to.
     * @param format HTML/XML/JSON/null/etc.
     * @param layout name of layout, <code>null</code> if no layout is needed.
     */
    void merge(Map<String, Object> values, String template, String layout, String format, Writer writer);


    /**
     * Same as {@link #merge(java.util.Map, String, String, String, java.io.Writer)}, but uses default layout and default format (html).
     * 
     * @param values values to be merged.
     * @param template name of template in format: <code>dir/template</code> without file extension. This is to support multiple template technologies in the future.
     * @param writer Writer to write results to.
     */
    void merge(Map<String, Object> values, String template, Writer writer);
    
    /**
     * @param values values to be merged.
     * @param template name of template in format: <code>dir/template</code> without file extension. This is to support multiple template technologies in the future.
     * @param layout name of layout, <code>null</code> if no layout is needed.
     * @param format HTML/XML/JSON
     * @param language The localisation - if used in the template renderer. Can be null.
     * @param writer Writer to write results to.
     * 
     * @see #merge(Map, String, String, String, Writer)
     * @author MTD
     */
    void merge(Map<String, Object> values, String template, String layout, String format, String language, Writer writer);
    
    /**
     * A template manager might need a context to be able to load templates from it.
     *
     * @param ctx servlet context
     */
    void setServletContext(ServletContext ctx);


    /**
     * @param templateLocation this can be absolute or relative.
     */
    void setTemplateLocation(String templateLocation);
    
}
