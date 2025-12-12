package com.lexia.backend.validation;

import com.lexia.backend.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user prompts before sending to AI.
 * Detects and blocks prompt injection attacks, SQL injection, and XSS attempts.
 * 
 * <p>Security measures:</p>
 * <ul>
 *   <li>Prompt injection detection (override attempts)</li>
 *   <li>SQL injection pattern blocking</li>
 *   <li>XSS/HTML script blocking</li>
 *   <li>Maximum length enforcement</li>
 *   <li>Control character filtering</li>
 * </ul>
 * 
 * @see ValidPrompt
 * @see ValidPromptValidator
 */
@Component
public class PromptSanitizer {

    private static final Logger log = LoggerFactory.getLogger(PromptSanitizer.class);

    /** Maximum allowed length for role-play prompts */
    public static final int MAX_ROLEPLAY_LENGTH = 500;

    /** Maximum allowed length for grammar prompts */
    public static final int MAX_GRAMMAR_LENGTH = 200;

    /** Default maximum length for general prompts */
    public static final int MAX_DEFAULT_LENGTH = 500;

    /**
     * Prompt injection patterns - attempts to override system instructions.
     * Case-insensitive patterns that indicate malicious intent.
     */
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            // Direct instruction override attempts
            Pattern.compile("ignore\\s+(all\\s+)?(previous|prior|above)\\s+(instructions?|prompts?|rules?)", 
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("ignore\\s+your\\s+(previous\\s+)?(instructions?|prompts?|rules?)", 
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("disregard\\s+(all\\s+)?(previous|prior|above)\\s+(instructions?|prompts?|rules?)", 
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("disregard\\s+your\\s+(prior|previous)\\s+(instructions?|prompts?|rules?)", 
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("forget\\s+(all\\s+)?(previous|prior|your)\\s+(instructions?|prompts?|context)", 
                    Pattern.CASE_INSENSITIVE),

            // New instruction injection
            Pattern.compile("new\\s+(instructions?|rules?|prompt)\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("system\\s*:\\s*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\[system\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\[INST\\]", Pattern.CASE_INSENSITIVE),

            // Role override attempts
            Pattern.compile("you\\s+are\\s+(now|no\\s+longer)\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("act\\s+as\\s+(if|though)\\s+you", Pattern.CASE_INSENSITIVE),
            Pattern.compile("pretend\\s+(to\\s+be|you\\s+are)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("switch\\s+(to|into)\\s+(a\\s+)?(different|new)\\s+(mode|role)", Pattern.CASE_INSENSITIVE),

            // Jailbreak patterns
            Pattern.compile("DAN\\s+mode", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bypass\\s+(filters?|safety|restrictions?)", Pattern.CASE_INSENSITIVE),

            // Output manipulation
            Pattern.compile("output\\s+(only|just)\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("respond\\s+(only\\s+)?with\\s+(yes|no|true|false)$", Pattern.CASE_INSENSITIVE)
    );

    /**
     * SQL injection patterns - attempts to manipulate database queries.
     * More specific patterns to avoid false positives on normal English.
     */
    private static final List<Pattern> SQL_INJECTION_PATTERNS = List.of(
            // Classic OR/AND injection: ' OR '1'='1, ' AND 1=1
            Pattern.compile("'\\s*(OR|AND)\\s+'?[^']*'?\\s*=\\s*'?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("UNION\\s+(ALL\\s+)?SELECT", Pattern.CASE_INSENSITIVE),
            Pattern.compile("SELECT\\s+.+\\s+FROM\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("INSERT\\s+INTO\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("UPDATE\\s+\\w+\\s+SET\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("DELETE\\s+FROM\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("DROP\\s+(TABLE|DATABASE|INDEX)\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("TRUNCATE\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ALTER\\s+TABLE\\s+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("EXEC(UTE)?\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("xp_cmdshell", Pattern.CASE_INSENSITIVE),
            Pattern.compile(";\\s*(DROP|DELETE|TRUNCATE)", Pattern.CASE_INSENSITIVE)
    );

    /**
     * XSS patterns - attempts to inject malicious scripts.
     */
    private static final List<Pattern> XSS_PATTERNS = List.of(
            Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE), // onclick, onerror, etc.
            Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<object[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE), // CSS expression
            Pattern.compile("url\\s*\\(\\s*['\"]?data:", Pattern.CASE_INSENSITIVE) // data: URLs
    );

    /**
     * Validates and sanitizes a prompt for AI processing.
     * Throws InvalidInputException if malicious content is detected.
     * 
     * @param input the user input to sanitize
     * @param maxLength maximum allowed length for this prompt type
     * @return sanitized input string
     * @throws InvalidInputException if input contains malicious patterns or exceeds length
     */
    public String sanitize(String input, int maxLength) {
        if (input == null) {
            return "";
        }

        // Trim whitespace
        String sanitized = input.trim();

        // Check length
        if (sanitized.length() > maxLength) {
            log.warn("Input exceeds maximum length: {} > {}", sanitized.length(), maxLength);
            throw new InvalidInputException(
                    String.format("Input too long. Maximum %d characters allowed.", maxLength));
        }

        // Check for prompt injection
        detectPromptInjection(sanitized);

        // Check for SQL injection
        detectSqlInjection(sanitized);

        // Check for XSS
        detectXss(sanitized);

        // Remove control characters (except newlines and tabs for formatting)
        sanitized = removeControlCharacters(sanitized);

        return sanitized;
    }

    /**
     * Convenience method using default maximum length.
     * 
     * @param input the user input to sanitize
     * @return sanitized input string
     * @throws InvalidInputException if input contains malicious patterns
     */
    public String sanitize(String input) {
        return sanitize(input, MAX_DEFAULT_LENGTH);
    }

    /**
     * Validates prompt without sanitization - just checks for malicious content.
     * 
     * @param input the user input to validate
     * @param maxLength maximum allowed length
     * @return true if input is valid, false otherwise
     */
    public boolean isValid(String input, int maxLength) {
        try {
            sanitize(input, maxLength);
            return true;
        } catch (InvalidInputException e) {
            return false;
        }
    }

    /**
     * Gets the validation error message for invalid input.
     * 
     * @param input the user input to validate
     * @param maxLength maximum allowed length
     * @return error message if invalid, null if valid
     */
    public String getValidationError(String input, int maxLength) {
        try {
            sanitize(input, maxLength);
            return null;
        } catch (InvalidInputException e) {
            return e.getMessage();
        }
    }

    /**
     * Detects prompt injection attempts.
     */
    private void detectPromptInjection(String input) {
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("Prompt injection detected: pattern={}", pattern.pattern());
                throw new InvalidInputException(
                        "Your message contains disallowed content. Please rephrase your question.");
            }
        }
    }

    /**
     * Detects SQL injection attempts.
     */
    private void detectSqlInjection(String input) {
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("SQL injection pattern detected: pattern={}", pattern.pattern());
                throw new InvalidInputException(
                        "Your message contains disallowed content. Please rephrase your question.");
            }
        }
    }

    /**
     * Detects XSS attempts.
     */
    private void detectXss(String input) {
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("XSS pattern detected: pattern={}", pattern.pattern());
                throw new InvalidInputException(
                        "Your message contains disallowed content. Please rephrase your question.");
            }
        }
    }

    /**
     * Removes control characters except tabs and newlines.
     */
    private String removeControlCharacters(String input) {
        // Keep: \t (tab), \n (newline), \r (carriage return for Windows)
        // Remove: other control chars (0x00-0x08, 0x0B, 0x0C, 0x0E-0x1F)
        return input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }
}
