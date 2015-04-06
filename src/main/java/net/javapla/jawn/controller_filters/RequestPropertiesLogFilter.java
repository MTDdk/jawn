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

package net.javapla.jawn.controller_filters;

import net.javapla.jawn.Context;


/**
 * Use this filter to log HTTP request properties to a log system.
 * request properties are not submitted parameters, but rather properties of
 * the request itself: HTTP method, URI, etc.
 *
 * @author Igor Polevoy
 */
public class RequestPropertiesLogFilter extends AbstractLoggingFilter{

    /**
     * Creates a filter with preset log level.
     *
     * @param level log level
     */
    
    public RequestPropertiesLogFilter(Level level) {
        super(level);
    }

    /**
     * Creates a filter with default "INFO" level.
     */
    public RequestPropertiesLogFilter() {
        super();    
    }

    @Override
    protected String getMessage(Context context) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("Request URL: ").append(context.requestUrl()).append("\n");
        sb.append("ContextPath: ").append(context.contextPath()).append("\n");
        sb.append("Query String: ").append(context.queryString()).append("\n");
        sb.append("URI Full Path: ").append(context.requestUri()).append("\n");
        sb.append("URI Path: ").append(context.path()).append("\n");
        sb.append("Method: ").append(context.method());
        return sb.toString();
    }
}
