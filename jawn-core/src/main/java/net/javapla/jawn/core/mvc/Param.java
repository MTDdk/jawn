package net.javapla.jawn.core.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.javapla.jawn.core.Context;

/**
 * Named parameter used in actions.
 * 
 * <p>Corresponds to link {@link Context#param(String)}
 * 
 * <p>Example usage:
 *
 * <pre>
 *   public class Controller {
 *     public void index(<b>@Param("date")</b> String date) {}
 *     public void index(<b>@Param("id")</b> Optional<String> id) {}
 *     ...
 *   }</pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    String value();
}
