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
package net.javapla.jawn.trash;

import java.net.URL;

import net.javapla.jawn.Context;
import net.javapla.jawn.exceptions.ControllerException;

/**
 * @author Igor Polevoy
 */
public class RedirectResponse extends ControllerResponse {
    private String path;

    protected RedirectResponse(Context context, URL url) {
        super(context);
        if(url == null) throw new IllegalArgumentException("url can't be null");
        this.path = url.toString();
    }

    protected RedirectResponse(Context context, String path) {
        super(context);
        if(path == null) throw new IllegalArgumentException("url can't be null");
        this.path = path;
    }

    @Override
    void doProcess() {
        try{
            context.responseSendRedirect(path);
        } catch(Exception e){
            throw new ControllerException(e);
        }
    }

    public String redirectValue() {
        return path;
    }
}
