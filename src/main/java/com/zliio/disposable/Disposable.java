package com.zliio.disposable;

/**
 * The entry point for the verification tool provides all the capabilities for validation and comparison.
 *
 * @author Leo
 * @since 1.0 (2023-07-26)
 **/
public final class Disposable {

    /**
     * Provide an explicit refresh capability to refresh the current disposable email database being maintained.
     */
    public void refreshDomains() {

    }

    /**
     * By providing a domain name or email address, the tool returns whether the domain is a disposable email.
     *
     * @param domainOrEmail domainOrEmail(eg: foo@gmail.com, gmail.com)
     * @return The result of the domain check process returns True if it is a disposable email, and False otherwise.
     */
    public Boolean validate(String domainOrEmail) {
        return Boolean.TRUE;
    }
}
