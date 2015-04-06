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

import net.javapla.jawn.Context;


/**
 * @author Igor Polevoy
 */
public abstract class ControllerResponse {

    private String contentType;
    private int status = 200;

//    private boolean statusSet = false;
//    private boolean contentTypeSet  = false;
    
    protected final Context context;
    
    public ControllerResponse(Context context) {
        this.context = context; // remove this, and set the contentType and status, when the response is put into the context instead
    }

    int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        context.setResponseStatus(status);
        this.status = status;
//        statusSet = true;
    }

    String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        context.setResponseContentType(contentType);
        this.contentType = contentType;
//        contentTypeSet = true;
    }
    
    /**
     * These are most likely obsolete and might be the context viewValues instead
     * Wait... this is 'protected' - what is its purpose?
     * @return
     */
    /*@Deprecated
    protected Map<String, Object> values(){
        return new HashMap<>();
    }*/

    final void process(){

        // this is already set earlier
//        if(!statusSet){
//            context.getHttpResponse().setStatus(status);
//        }
//        if(!contentTypeSet){
//            context.getHttpResponse().setContentType(contentType);
//        }
        doProcess();
    }

    abstract void doProcess();
}