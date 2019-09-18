package net.javapla.jawn.core.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.javapla.jawn.core.Context;

/**
 * Named path parameter used in actions.
 * 
 * <p>Corresponds to link {@link Context.Request#pathParam(String)}
 * 
 * <p>Example usage:
 *
 * <pre>
 *   public class Controller {
 *     public void index(<b>@PathParam("date")</b> String date) {}
 *     public void index(<b>@PathParam("id")</b> Optional<String> id) {}
 *     ...
 *   }</pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathParam {
    String value();
}
