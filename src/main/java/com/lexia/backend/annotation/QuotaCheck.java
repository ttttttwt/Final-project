package com.lexia.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark AI endpoints that require quota checking.
 * When applied, the system will verify that the user has sufficient quota
 * before allowing the request to proceed.
 * 
 * <p>
 * Usage:
 * </p>
 * 
 * <pre>
 * {@code @QuotaCheck(contentType = "roleplay", incrementSession = true)}
 * public ResponseEntity<ScenarioDTO> createScenario(@AuthenticationPrincipal User user) {
 *     // ...
 * }
 * </pre>
 * 
 * @see com.lexia.backend.aspect.QuotaCheckAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuotaCheck {

    /**
     * The content type being checked (roleplay, grammar, flashcard, etc.).
     * Must match constants in AiUsageTracker.
     */
    String contentType();

    /**
     * If true, increments the session counter (for roleplay sessions, flashcard
     * decks, grammar exercises).
     * Set to true for operations that create new "sessions" (e.g., starting a new
     * roleplay).
     */
    boolean incrementSession() default false;

    /**
     * Session type for incrementing: "roleplay", "flashcard", "grammar".
     * Only used when incrementSession is true.
     */
    String sessionType() default "";

    /**
     * Custom message to show when quota is exceeded.
     */
    String quotaExceededMessage() default "";
}
