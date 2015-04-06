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
package net.javapla.jawn.templatemanagers.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import net.javapla.jawn.exceptions.ViewException;
import net.javapla.jawn.util.StringUtil;
import freemarker.template.TemplateModel;

/**
 * @author Igor Polevoy
 */
public class YieldTag extends FreeMarkerTag {
    
    @Override
    protected void render(Map<String, TemplateModel> params, String body, Writer writer) throws IOException {
        validateParamsPresence(params, "to");
        String nameOfContent = params.get("to").toString();


        Map<String, List<String>> allContent = ContentTL.getAllContent();
        if(allContent == null){
            throw new ViewException("Content for name: '" + nameOfContent + "' is missing. " +
                    "Ensure you have this tag <@content for=\"title\">... on page being rendered.");
        }
        List<String>  contentList = ContentTL.getAllContent().get(nameOfContent);

        if(contentList == null){
            logger().debug("Failed to find content for: " + nameOfContent);
        }else{
            writer.write(StringUtil.join(contentList, " "));
        }
    }
}