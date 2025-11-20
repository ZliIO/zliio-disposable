package com.zliio.disposable.core;

import com.zliio.disposable.BloomFilter;
import com.zliio.disposable.Disposable;
import com.zliio.disposable.DisposableChecker;
import com.zliio.disposable.DisposableDomainLoader;
import com.zliio.disposable.loader.BuiltinDomainLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * A high-performance, memory-efficient implementation of the {@link Disposable} interface
 * that uses a Bloom filter to detect disposable email domains.
 * <p>
 * A Bloom filter is a probabilistic data structure that is highly space-efficient for
 * testing whether an element is a member of a set. This implementation is ideal for
 * applications that need to check against a very large list of disposable domains
 * without consuming a large amount of memory.
 *
 * <h3>Important Trade-offs: False Positives</h3>
 * This implementation guarantees <b>no false negatives</b> (if a domain is in the list,
 * it will always be detected). However, it may produce <b>false positives</b>—it might
 * incorrectly flag a legitimate domain as disposable. The probability of this is
 * configured to be extremely low (e.g., 0.00001) but is never zero. This trade-off is
 * generally acceptable for anti-spam and anti-abuse systems.
 *
 * <pre>{@code
 * // Example usage with the built-in domain list:
 * Disposable disposableChecker = new BloomFilterDisposable();
 * boolean isDisposable = disposableChecker.validate("user@mailinator.com"); // returns true
 *
 * // Example with a custom domain loader:
 * DisposableDomainLoader myLoader = () -> Set.of("tempmail.com", "trashmail.org");
 * Disposable customChecker = new BloomFilterDisposable(myLoader);
 * boolean isCustomDisposable = customChecker.validate("contact@tempmail.com"); // returns true
 * }</pre>
 *
 * @author 1TSC
 * @version 1.0
 * @see Disposable
 * @see DisposableDomainLoader
 * @since 2025/11/13
 */
public final class BloomFilterDisposable implements Disposable {
    private static final Logger log = LoggerFactory.getLogger(BloomFilterDisposable.class);
    // Corrected typo and made final for immutability.
    private final BloomFilter<String> disposableBloomFilter;

    /**
     * Constructs a new instance using the default {@link BuiltinDomainLoader}.
     * The Bloom filter will be initialized with the standard set of disposable domains.
     */
    public BloomFilterDisposable() {
        this(new BuiltinDomainLoader());
    }

    /**
     * Constructs a new instance using a custom domain loader.
     * This allows for loading disposable domains from any source, such as a file,
     * a database, or a remote API.
     *
     * @param domainLoader The {@link DisposableDomainLoader} used to provide the set of domains.
     *                     Must not be null.
     */
    public BloomFilterDisposable(DisposableDomainLoader domainLoader) {
        Objects.requireNonNull(domainLoader, "DomainLoader must not be null.");
        Set<String> domainSet = domainLoader.getDomains();
        if (domainSet.isEmpty()) {
            log.warn("Initializing Bloom filter with an empty domain set. All checks will return false.");
        }
        // Configure the Bloom filter with the size of the domain set and a low false positive probability.
        this.disposableBloomFilter = new BloomFilter<>(domainSet.size(), 0.00001);
        // Populate the filter, ensuring all domains are normalized.
        for (String domain : domainSet) {
            if (domain != null && !domain.trim().isEmpty()) {
                this.disposableBloomFilter.add(domain.trim().toLowerCase());
            }
        }
        log.info("Bloom filter initialized with {} domains. Configuration: {}",
                domainSet.size(), this.disposableBloomFilter.getConfig());
    }

    /**
     * Validates if the given string, which can be an email address or a domain,
     * corresponds to a disposable service.
     *
     * @param domainOrEmail The email address or domain name to validate.
     * @return {@code true} if the input is identified as disposable, {@code false} otherwise.
     */
    @Override
    public boolean validate(String domainOrEmail) {
        if (domainOrEmail == null || domainOrEmail.trim().isEmpty()) {
            return false;
        }
        // Unified logic: extract domain and check it.
        return DisposableChecker.getDomainFromEmail(domainOrEmail)
                .map(this::isDisposableDomain)
                .orElse(false);
    }

    /**
     * Validates if the given email address belongs to a disposable domain.
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
     * Validates if the given domain is a disposable domain.
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
     * The core private method that performs the check against the Bloom filter.
     * It normalizes the input domain before querying the filter.
     *
     * @param domain The domain to check (must not be null).
     * @return {@code true} if the domain might be in the disposable set.
     */
    private boolean isDisposableDomain(String domain) {
        // Centralized normalization and checking logic.
        return disposableBloomFilter.mightContain(domain.trim().toLowerCase());
    }
}