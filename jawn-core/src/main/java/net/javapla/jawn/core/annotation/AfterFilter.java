package net.javapla.jawn.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.javapla.jawn.core.Route;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD })
public @interface AfterFilter {

    Class<? extends Route.After> value();
}
