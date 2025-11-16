package com.zliio.disposable;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A utility class for validating and classifying strings as email addresses or domains.
 * <p>
 * This class provides static methods to check if a given string is a valid email,
 * a valid domain, or to extract the domain part from an email address.
 * It is designed as a final class with a private constructor to prevent instantiation.
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
public final class DisposableChecker {
    /**
     * A pre-compiled pattern for validating email addresses based on a common regex.
     * The pattern is case-insensitive.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE
    );
    /**
     * A pre-compiled pattern for validating domain names.
     * The pattern is case-insensitive.
     */
    private static final Pattern DOMAIN_PATTERN =
            Pattern.compile("^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}$",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Represents the type of input string after classification.
     */
    public enum InputType {
        /**
         * The input string is a valid email address.
         */
        EMAIL,
        /**
         * The input string is a valid domain name.
         */
        DOMAIN,
        /**
         * The input string is neither a valid email nor a valid domain.
         */
        INVALID
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DisposableChecker() {
        // This class should not be instantiated.
    }

    /**
     * Classifies a given string as an email, a domain, or invalid.
     *
     * @param input The string to be classified.
     * @return The {@link InputType} classification (EMAIL, DOMAIN, or INVALID).
     */
    public static InputType classify(String input) {
        if (isStringBlank(input)) {
            return InputType.INVALID;
        }
        if (EMAIL_PATTERN.matcher(input).matches()) {
            return InputType.EMAIL;
        }
        if (DOMAIN_PATTERN.matcher(input).matches()) {
            return InputType.DOMAIN;
        }
        return InputType.INVALID;
    }

    /**
     * Checks if the given string is a valid email address.
     *
     * @param input The string to check.
     * @return {@code true} if the string is a valid email address, {@code false} otherwise.
     */
    public static boolean isEmail(String input) {
        if (isStringBlank(input)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(input).matches();
    }

    /**
     * Checks if the given string is a valid domain name.
     *
     * @param input The string to check.
     * @return {@code true} if the string is a valid domain name, {@code false} otherwise.
     */
    public static boolean isDomain(String input) {
        if (isStringBlank(input)) {
            return false;
        }
        return DOMAIN_PATTERN.matcher(input).matches();
    }

    /**
     * Extracts the domain part from a valid email address string.
     * <p>
     * This method first validates if the input is a well-formed email address.
     * If valid, it returns the domain portion. Otherwise, it returns an empty Optional.
     *
     * @param emailAddress The potential email address string.
     * @return An {@link Optional<String>} containing the domain if the input is a valid email,
     * or {@link Optional#empty()} otherwise.
     */
    public static Optional<String> getDomainFromEmail(String emailAddress) {
        if (!isEmail(emailAddress)) {
            return Optional.empty();
        }
        // At this point, we know emailAddress is not null or blank and is a valid email.
        int atIndex = emailAddress.lastIndexOf('@');
        return Optional.of(emailAddress.substring(atIndex + 1));
    }

    /**
     * A helper method to check if a string is null, empty, or consists only of whitespace.
     * This is a simplified backport of String.isBlank() for compatibility or consistency.
     *
     * @param str The string to check.
     * @return {@code true} if the string is null or blank, {@code false} otherwise.
     */
    private static boolean isStringBlank(String str) {
        // For Java 11+, simply use: return str == null || str.isBlank();
        return str == null || str.trim().isEmpty();
    }
}
