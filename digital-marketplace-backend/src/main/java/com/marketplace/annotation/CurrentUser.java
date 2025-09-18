package com.marketplace.annotation;

import java.lang.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
/**
 * Custom annotation to access the currently authenticated user in controller methods.
 * Usage: public ResponseEntity<?> someMethod(@CurrentUser UserPrincipal currentUser) { ... }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
    boolean required() default true;
}