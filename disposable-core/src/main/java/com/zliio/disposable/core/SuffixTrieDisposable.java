package com.zliio.disposable.core;

import com.zliio.disposable.*;
import com.zliio.disposable.loader.BuiltinDomainLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * An implementation of the {@link Disposable} interface that utilizes a <b>Suffix Trie</b>
 * (also known as a reversed Trie or Radix Tree) for domain validation.
 * <p>
 * This data structure is highly optimized for matching domain suffixes. Its primary advantage
 * is the ability to efficiently detect not only exact domain matches but also any subdomains
 * of a registered disposable parent domain. For example, if {@code "temp-mail.org"} is added
 * to the trie, this implementation will automatically identify {@code "sub.temp-mail.org"} and
 * {@code "another.sub.temp-mail.org"} as disposable without needing explicit entries for them.
 *
 * <h3>Key Characteristics:</h3>
 * <ul>
 *   <li><b>No False Positives:</b> Unlike a Bloom filter, this implementation is deterministic and
 *       guarantees zero false positives.</li>
 *   <li><b>Subdomain Matching:</b> Inherently handles subdomain blocking, which is a common
 *       tactic used by disposable email providers.</li>
 *   <li><b>Memory Efficient:</b> More memory-efficient than a simple HashSet for large sets of
 *       domains with shared suffixes (e.g., {@code temp.com}, {@code mail.com}).</li>
 * </ul>
 *
 * <pre>{@code
 * // Example usage:
 * DisposableDomainLoader loader = () -> Set.of("mailinator.com", "temp-mail.org");
 * Disposable disposableChecker = new SuffixTrieDisposable(loader);
 *
 * // Exact match
 * boolean isDisposable1 = disposableChecker.validate("user@mailinator.com"); // returns true
 *
 * // Subdomain match
 * boolean isDisposable2 = disposableChecker.validate("contact@sub.temp-mail.org"); // returns true
 *
 * // Non-disposable domain
 * boolean isDisposable3 = disposableChecker.validate("user@gmail.com"); // returns false
 * }</pre>
 *
 * @author 1TSC
 * @version 1.0
 * @see Disposable
 * @see DisposableDomainLoader
 * @see DomainSuffixTrie
 * @since 2025/11/13
 */
public final class SuffixTrieDisposable implements Disposable {
    private static final Logger log = LoggerFactory.getLogger(SuffixTrieDisposable.class);
    private final DomainSuffixTrie domainSuffixTrie;

    /**
     * Constructs a new instance using the default {@link BuiltinDomainLoader}.
     * The Suffix Trie will be initialized with the standard set of disposable domains.
     */
    public SuffixTrieDisposable() {
        this(new BuiltinDomainLoader());
    }

    /**
     * Constructs a new instance using a custom domain loader.
     * This allows for loading disposable domains from any source.
     *
     * @param domainLoader The {@link DisposableDomainLoader} used to provide the set of domains.
     *                     Must not be null.
     */
    public SuffixTrieDisposable(DisposableDomainLoader domainLoader) {
        Objects.requireNonNull(domainLoader, "DomainLoader must not be null.");
        Set<String> domainSet = domainLoader.getDomains();
        if (domainSet.isEmpty()) {
            log.warn("Initializing suffix trie with an empty domain set. All checks will return false.");
        }
        this.domainSuffixTrie = new DomainSuffixTrie();
        for (String domain : domainSet) {
            if (domain != null && !domain.trim().isEmpty()) {
                // Normalize domains before adding to ensure consistent matching.
                this.domainSuffixTrie.add(domain.trim().toLowerCase());
            }
        }
        // Corrected the log message to accurately reflect the data structure being used.
        log.info("Suffix trie initialized with {} domains/rules.", this.domainSuffixTrie.size());
    }

    /**
     * Validates if the given string, which can be an email address or a domain,
     * corresponds to a disposable service by checking its suffix against the trie.
     *
     * @param domainOrEmail The email address or domain name to validate.
     * @return {@code true} if the input is identified as disposable, {@code false} otherwise.
     */
    @Override
    public boolean validate(String domainOrEmail) {
        if (domainOrEmail == null || domainOrEmail.trim().isEmpty()) {
            return false;
        }
        // Unified logic: extract the domain part and check it.
        return DisposableChecker.getDomainFromEmail(domainOrEmail)
                .map(this::isDisposableDomain)
                .orElse(false);
    }

    /**
     * Validates if the given email address belongs to a disposable domain or a subdomain thereof.
     *
     * @param email The email address to validate.
     * @return {@code true} if the email's domain is identified as disposable, {@code false} otherwise.
     */
    @Override
    public boolean validateEmail(String email) {
        // Delegate to the unified validation logic.
        return validate(email);
    }

    /**
     * Validates if the given domain is a disposable domain or a subdomain thereof.
     *
     * @param domain The domain name to validate.
     * @return {@code true} if the domain is identified as disposable, {@code false} otherwise.
     */
    @Override
    public boolean validateDomain(String domain) {
        if (!DisposableChecker.isDomain(domain)) {
            return false;
        }
        return isDisposableDomain(domain);
    }

    /**
     * The core private method that performs the check against the Suffix Trie.
     * It normalizes the input domain before querying the trie.
     *
     * @param domain The domain to check (must not be null).
     * @return {@code true} if the domain or one of its parent domains exists in the trie.
     */
    private boolean isDisposableDomain(String domain) {
        // Centralized normalization and checking logic.
        return domainSuffixTrie.matches(domain.trim().toLowerCase());
    }
}