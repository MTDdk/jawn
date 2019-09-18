package net.javapla.jawn.core.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.javapla.jawn.core.Context;

/**
 * Named query parameter used in actions.
 * 
 * <p>Corresponds to link {@link Context.Request#queryParam(String)}
 * 
 * <p>Example usage:
 *
 * <pre>
 *   public class Controller {
 *     public void index(<b>@QueryParam("date")</b> String date) {}
 *     public void index(<b>@QueryParam("id")</b> Optional<String> id) {}
 *     ...
 *   }</pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryParam {
    String value();
}
