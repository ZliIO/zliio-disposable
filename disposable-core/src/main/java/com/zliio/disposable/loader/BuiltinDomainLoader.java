package com.zliio.disposable.loader;

import com.zliio.disposable.DisposableDomainLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link DisposableDomainLoader} that loads a predefined list of domains
 * from a resource file bundled within the application's classpath.
 * <p>
 * This implementation uses a <b>lazy-loading</b> and <b>caching</b> strategy. The domain
 * list is loaded from the classpath resource only on the first call to {@link #getDomains()}.
 * The result is then cached in an immutable set for all subsequent calls on the same instance,
 * ensuring high performance after the initial load.
 * <p>
 * This approach avoids upfront memory allocation, consuming resources only when the
 * domain list is actually needed. The initialization is implemented in a thread-safe
 * manner using the double-checked locking pattern.
 * <p>
 * If the resource file cannot be found or read, the first call to {@code getDomains()}
 * will throw an {@link IllegalStateException}, clearly indicating a fatal configuration
 * or packaging error at runtime.
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
public class BuiltinDomainLoader implements DisposableDomainLoader {
    private static final String BUILTIN_DOMAINS_RESOURCE_PATH = "/disposable/domains.txt";
    // The 'volatile' keyword is crucial for the correctness of double-checked locking.
    // It ensures that writes to this variable are atomically visible to other threads.
    private volatile Set<String> domains;

    /**
     * Constructs a new BuiltinDomainLoader. The domain data is not loaded
     * at construction time but will be loaded on the first request.
     */
    public BuiltinDomainLoader() {
        // The 'domains' field is intentionally left null here for lazy loading.
    }

    /**
     * Returns an immutable set of built-in disposable email domains.
     * <p>
     * On the first call, this method loads domains from the classpath resource,
     * normalizes them, and caches them in an unmodifiable set. Subsequent calls
     * return the cached set instantly without any I/O operations. This method
     * is thread-safe.
     *
     * @return An unmodifiable {@link Set} of disposable domains.
     * @throws IllegalStateException if the underlying resource file cannot be found or read
     *                               during the initial loading attempt.
     */
    @Override
    public Set<String> getDomains() {
        // First check (no lock): The most common path for performance.
        if (domains == null) {
            // Synchronized block to ensure only one thread can initialize.
            synchronized (this) {
                // Second check (with lock): Ensures initialization happens only once.
                if (domains == null) {
                    log.info("Lazy-loading built-in domains from '" + BUILTIN_DOMAINS_RESOURCE_PATH + "'...");
                    domains = loadDomainsFromResource();
                }
            }
        }
        return domains;
    }

    /**
     * Internal helper method to perform the actual loading.
     *
     * @return The loaded and processed set of domains.
     * @throws IllegalStateException wrapping an IOException on failure.
     */
    private Set<String> loadDomainsFromResource() {
        try (InputStream is = getClass().getResourceAsStream(BUILTIN_DOMAINS_RESOURCE_PATH)) {
            if (is == null) {
                throw new IOException("Classpath resource not found: " + BUILTIN_DOMAINS_RESOURCE_PATH);
            }
            Set<String> loadedDomains = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toSet());
            log.info("Successfully loaded {} built-in disposable domains.", loadedDomains.size());
            return Collections.unmodifiableSet(loadedDomains);
        } catch (IOException e) {
            log.error("Fatal: Failed to load built-in disposable domains. {}", e.getMessage());
            // An unchecked exception is appropriate here, as this indicates a serious
            // application packaging or runtime environment error.
            throw new IllegalStateException("Failed to initialize BuiltinDomainLoader from resource", e);
        }
    }
}