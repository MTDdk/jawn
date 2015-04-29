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

package net.javapla.jawn.core;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.javapla.jawn.core.http.SessionHelper;
import net.javapla.jawn.core.util.CollectionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Igor Polevoy
 */
@Deprecated
class ParamCopy {
    private static Logger logger = LoggerFactory.getLogger(ParamCopy.class.getName());


    static void copyInto(HttpServletRequest request, ControllerResponse response, PropertiesImpl properties){
        Map<String, Object> assigns = response.getViewObjects();
        insertActiveWebParamsInto(assigns, request, properties);
        copyRequestAttributesInto(assigns, request);
        copyRequestParamsInto(assigns, request);
        copySessionAttrsInto(assigns, request);
        copyRequestProperties(assigns, request);
    }

    private static void insertActiveWebParamsInto(Map<String, Object> assigns, HttpServletRequest request, /*ContextImpl context,*/ PropertiesImpl properties) {
        assigns.put("context_path", request.getContextPath());
        //in some cases the Route is missing - for example, when exception happened before Router was invoked.

        Map<Object, Object> params = CollectionUtil.map("environment", properties.getMode());

//        if(context.getRoute() != null){
//            params.put("controller", context.getRoute().getControllerPath());
//            params.put("action", context.getRoute().getAction());
//            params.put("language", context.getRouteLanguage());
//        }
        assigns.put("activeweb", params);
    }


    private static void copySessionAttrsInto(Map<String, Object> assigns, HttpServletRequest request) {

        Map<String, Object> sessionAttrs = SessionHelper.getSessionAttributes(request);
        if (assigns.get("session") != null) {
            logger.warn("found 'session' value set by controller. It is reserved by ActiveWeb and will be overwritten.");
        }
        if (sessionAttrs.containsKey("flasher")){ //flasher is special
            assigns.put("flasher", sessionAttrs.get("flasher"));
        }
        assigns.put("session", sessionAttrs);
    }


    private static void copyRequestParamsInto(Map<String, Object> assigns, HttpServletRequest request) {
        Enumeration<String> names = request.getParameterNames();

        Map<String, String> requestParameterMap = new HashMap<String, String>();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            String[] values = request.getParameterValues(name.toString());
            Object value = values != null && values.length == 1 ? values[0] : values;
            if(value != null)
                requestParameterMap.put(name.toString(), value.toString());
        }
        assigns.put("request", requestParameterMap);
    }


    private static void copyRequestAttributesInto(Map<String, Object> assigns, HttpServletRequest request){
        Enumeration<String> names = request.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = request.getAttribute(name.toString());
            assigns.put(name, value);
        }
    }

    private static void copyRequestProperties(Map<String, Object> assigns, HttpServletRequest request) {
        assigns.put("request_props", CollectionUtil.map("url", request.getRequestURL().toString()));
    }
}
