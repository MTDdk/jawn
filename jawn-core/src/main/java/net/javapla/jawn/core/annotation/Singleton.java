package net.javapla.jawn.core.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.javapla.jawn.core.internal.injection.Injector;


/**
 * Apply this to implementation classes when you want only one instance (per {@link Injector}) to be
 * reused for all injections for that binding.
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RUNTIME)
public @interface Singleton {}
