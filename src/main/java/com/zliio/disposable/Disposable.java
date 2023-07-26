package com.zliio.disposable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The entry point for the verification tool provides all the capabilities for validation and comparison.
 *
 * @author Leo
 * @since 1.0 (2023-07-26)
 **/
public final class Disposable {

    /**
     * EMAIL_PATTERN.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Provide an explicit refresh capability to refresh the current disposable email database being maintained.
     */
    public void refreshDisposableDomains() {
        DomainsLibraryManager.refreshDisposableDomains();
    }

    /**
     * By providing a domain name or email address, the tool returns whether the domain is a disposable email.
     *
     * @param domainOrEmail domainOrEmail(eg: foo@gmail.com, gmail.com)
     * @return The result of the domain check process returns False if it is a disposable email, and True otherwise.
     */
    public Boolean validate(String domainOrEmail) {
        if (domainOrEmail.contains("@") && isNotValidEmail(domainOrEmail)) {
            return Boolean.FALSE;
        }
        int lastIndex = domainOrEmail.lastIndexOf('@');
        String domain = domainOrEmail;
        if (lastIndex >= 0) {
            domain = domainOrEmail.substring(lastIndex + 1);
        }
        return !DomainsLibraryManager.containDomain(domain);
    }

    public Boolean validateEmail(String email) {
        if (isNotValidEmail(email)) {
            return Boolean.FALSE;
        }
        int lastIndex = email.lastIndexOf('@');
        String domain = "";
        if (lastIndex >= 0) {
            domain = email.substring(lastIndex + 1);
        } else {
            return Boolean.FALSE;
        }
        return !DomainsLibraryManager.containDomain(domain);
    }

    public Boolean validateDomain(String domain) {
        return !DomainsLibraryManager.containDomain(domain);
    }

    public boolean isNotValidEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return !matcher.matches();
    }

    public void addBlackDomain(String domain) {
        DomainsLibraryManager.addBlackDomain(domain);
    }

    public void addWhiteDomain(String domain) {
        DomainsLibraryManager.addWhiteDomain(domain);
    }

    public void removeBlackDomain(String domain) {
        DomainsLibraryManager.removeBlackDomain(domain);
    }

    public void removeWhiteDomain(String domain) {
        DomainsLibraryManager.removeWhiteDomain(domain);
    }

}
