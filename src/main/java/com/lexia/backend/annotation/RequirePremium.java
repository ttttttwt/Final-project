package com.lexia.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require Premium (Pro) subscription.
 * 
 * <p>
 * When applied to a controller method, the PremiumCheckAspect will verify
 * that the authenticated user has an active Pro subscription (MONTHLY or
 * YEARLY).
 * </p>
 * 
 * <p>
 * If the user is not authenticated or not a Pro subscriber, a 403 Forbidden
 * response is returned.
 * </p>
 *
 * @since Sprint 5
 * @see com.lexia.backend.aspect.PremiumCheckAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePremium {

    /**
     * Custom message to return if user is not Premium.
     */
    String message() default "This feature requires a Pro subscription";
}
