package com.lexia.backend.util;

import com.lexia.backend.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Utility class for validating timezone and language codes.
 */
public class ValidationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationUtils.class);

    // Common ISO 639-1 language codes
    private static final Set<String> VALID_LANGUAGE_CODES = new HashSet<>(Arrays.asList(
            "en", "vi", "es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh", "ar", "hi", "th", "id"));

    /**
     * Validates if the given timezone is a valid IANA timezone identifier.
     *
     * @param timezone the timezone to validate
     * @throws InvalidInputException if timezone is invalid
     */
    public static void validateTimezone(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            throw new InvalidInputException("Timezone cannot be null or empty");
        }

        try {
            ZoneId.of(timezone);
            LOG.debug("Validated timezone: {}", timezone);
        } catch (DateTimeException e) {
            LOG.warn("Invalid timezone: {}", timezone);
            throw new InvalidInputException(
                    "Invalid timezone: " + timezone + ". Must be a valid IANA timezone (e.g., UTC, Asia/Ho_Chi_Minh)");
        }
    }

    /**
     * Validates if the given language code is a valid ISO 639-1 code.
     *
     * @param language the language code to validate
     * @throws InvalidInputException if language code is invalid
     */
    public static void validateLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            throw new InvalidInputException("Language code cannot be null or empty");
        }

        if (!language.matches("^[a-z]{2}$")) {
            throw new InvalidInputException("Language code must be a 2-letter ISO 639-1 code (e.g., en, vi)");
        }

        // Check if it's a valid locale
        try {
            Locale locale = new Locale(language);
            if (!VALID_LANGUAGE_CODES.contains(language.toLowerCase())) {
                LOG.warn("Language code {} is not in the common supported list, but is valid", language);
            }
            LOG.debug("Validated language: {}", language);
        } catch (Exception e) {
            LOG.warn("Invalid language code: {}", language);
            throw new InvalidInputException("Invalid language code: " + language);
        }
    }

    /**
     * Validates phone number format.
     *
     * @param phoneNumber the phone number to validate
     * @throws InvalidInputException if phone number is invalid
     */
    public static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return; // Phone number is optional
        }

        if (!phoneNumber.matches("^[+]?[0-9]{10,20}$")) {
            throw new InvalidInputException("Phone number must be 10-20 digits, optionally starting with +");
        }
        LOG.debug("Validated phone number format");
    }

    /**
     * Validates bio length.
     *
     * @param bio the bio to validate
     * @throws InvalidInputException if bio exceeds max length
     */
    public static void validateBio(String bio) {
        if (bio != null && bio.length() > 500) {
            throw new InvalidInputException("Bio must not exceed 500 characters");
        }
    }
}
