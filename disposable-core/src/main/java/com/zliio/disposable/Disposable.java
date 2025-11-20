package com.zliio.disposable;

/**
 * Disposable email validator interface
 * Provides the capability to validate whether an email address or domain belongs to a disposable email service
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/12
 */
public interface Disposable {
    /**
     * Validates whether a domain or email address belongs to a disposable email service
     * Supports both email addresses and domain names as input
     *
     * @param domainOrEmail domain name or email address to validate (e.g., foo@gmail.com, gmail.com)
     * @return validation result - returns false if it's a disposable email, true otherwise
     * @see #validateEmail(String)
     * @see #validateDomain(String)
     */
    boolean validate(String domainOrEmail);

    /**
     * Validates whether an email address belongs to a disposable email service
     * This method extracts the domain from the email address and validates it
     *
     * @param email complete email address to validate (e.g., foo@gmail.com)
     * @return validation result - returns false if it's a disposable email, true otherwise
     */
    boolean validateEmail(String email);

    /**
     * Validates whether a domain is known to provide disposable email services
     * Directly validates the domain against known disposable email service providers
     *
     * @param domain domain name to validate (e.g., gmail.com)
     * @return validation result - returns false if the domain provides disposable email services, true otherwise
     */
    boolean validateDomain(String domain);
}