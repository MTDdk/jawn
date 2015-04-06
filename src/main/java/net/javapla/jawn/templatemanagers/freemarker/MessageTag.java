    package net.javapla.jawn.templatemanagers.freemarker;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.javapla.jawn.Messages;
import net.javapla.jawn.util.StringUtil;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;

/**
 * The message tag is designed to display messages in view templates. Message values are defined in resource bundle called
 * "activeweb_messages". This means that  this tag will be looking for file called <code>activeweb_messages.properties</code> as default
 * name and others, such as <code>activeweb_messages_fr_FR.properties</code> in case French locale was specified.
 *
 * <p>
 *     Examples:
 * </p>
 *
 * <strong><i>Simple usage</i></strong><br>
 *  Given that there is a file <code>activeweb_messages.properties</code> with content:<br>
 *  <pre>
greeting=Hello!
 *  </pre>
 *  and tag code:
 *
 *  <pre>
&lt;@message key=&quot;greeting&quot;/&gt;
 *  </pre>
 *  then the output will be:
 *  <pre>
Hello!
 *  </pre>
 *
 * <strong><i>Message with parameters</i></strong><br>
 * Lets say a message in resource bundle is declared like this:
 *
 * <pre>
meeting=Meeting will take place on {0} at {1}
 * </pre>
 *
 * You can then specify the tag with parameters:
 * <pre>
&lt;@message key=&quot;meeting&quot; param0=&quot;Wednesday&quot; param1=&quot;2:00 PM&quot;/&gt;
 * </pre>
 *
 * When a view template renders, the outcome will be:
 *
 * <pre>
Meeting will take place on Wednesday at 2:00 PM
 * </pre>
 *
 *  <strong><i>Defaulting to key if value not found</i></strong><br>
 *
 *  In case a resource bundle does not have a key specified, the key is rendered as value verbatim:
 *
 *  <pre>
&lt;@message key=&quot;greeting&quot;/&gt;
 *  </pre>
 *
 *  The output:
 *  <pre>
greeting
 *  </pre>
 *
 *
 * <strong><i>Detection of locale from request</i></strong><br>
 *
 * If there is a locale on the request supplied by the agent, then this locale is automatically picked up by this tag.
 * For instance, if a browser supplies locale "fr_FR" and there is a corresponding resource bundle:
 * "activeweb_messages_fr_FR.properties", with this property:
 *
 * <pre>
greeting=Bonjour!
 * </pre>
 *
 * then this tag:
 * <pre>
&lt;@message key=&quot;greeting&quot;/&gt;
 * </pre>
 * will produce:
 * <pre>
Bonjour!
 * </pre>
 *
 * <strong><i>Overriding request locale</i></strong><br>
 * There is a "locale" argument you can pass to the tag to override the locale from request:
 * <pre>
&lt;@message key=&quot;greeting&quot; locale=&quot;de_DE&quot;/&gt;
 * </pre>
 *
 *
 *
 * @author Igor Polevoy: 8/15/12 3:50 PM
 */
public class MessageTag extends FreeMarkerTag {

    @Override
    protected void render(Map<String, TemplateModel> params, String body, Writer writer) throws Exception {
        if (params.containsKey("key")) {
            String key = params.get("key").toString();
            if(params.containsKey("locale")){
                String localeString = params.get("locale").toString();
                String language, country;
                Locale locale;
                if(localeString.contains("_")){
                    language = StringUtil.split(localeString, '_')[0];
                    country = StringUtil.split(localeString, '_')[1];
                    locale = new Locale(language, country);
                }else{
                    language = localeString;
                    locale = new Locale(language);
                }
                writer.write(Messages.message(key, locale, getParamsArray(params)));
            }else{
                writer.write(Messages.message(key, Locale.ENGLISH, getParamsArray(params)));
            }
        }else{
             writer.write("<span style=\"display:none\">you failed to supply key for this message tag</span>");
        }
    }


    private String[] getParamsArray(Map<?, ?> params) {

        int index = 0;
        List<String> paramList = new ArrayList<String>();
        for (String paramName = "param"; params.containsKey(paramName + index); index++){
            String param = ((SimpleScalar) params.get(paramName + index)).getAsString();
            paramList.add(param);
        }
        return paramList.toArray(new String[]{});
    }
}