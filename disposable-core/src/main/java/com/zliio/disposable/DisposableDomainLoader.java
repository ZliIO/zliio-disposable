package com.zliio.disposable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A strategy interface for loading disposable email domains.
 * <p>
 * This interface defines a uniform contract for retrieving a set of disposable email domains.
 * Disposable emails are often used to bypass registration verification. Identifying these domains
 * is crucial for improving user quality, preventing spam, and combating abuse.
 * <p>
 * As a {@link FunctionalInterface}, it can be easily implemented using a lambda expression,
 * allowing for flexible loading from various data sources such as in-memory collections,
 * files, databases, or network APIs.
 *
 * <pre>{@code
 * // Example: creating a loader from a static Set
 * DisposableDomainLoader staticLoader = DisposableDomainLoader.of(Set.of("mailinator.com", "yopmail.com"));
 *
 * // Example: loading from a file with graceful IO exception handling
 * DisposableDomainLoader fileLoader = DisposableDomainLoader.fromFile(Paths.get("disposable_domains.txt"));
 * Set<String> domains = fileLoader.getDomains();
 *
 * // Example: custom implementation with a lambda
 * DisposableDomainLoader customLoader = () -> fetchDomainsFromApi();
 * }</pre>
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
@FunctionalInterface
public interface DisposableDomainLoader {

    /**
     * Slf4j Logger.
     */
    Logger log = LoggerFactory.getLogger(DisposableDomainLoader.class);

    /**
     * Gets the set of disposable email domains.
     * <p>
     * Implementations of this method should be thread-safe. The returned set should be
     * immutable or its write operations thread-safe to prevent concurrent modification issues
     * in a multi-threaded environment.
     *
     * @return A {@link Set} containing all disposable domains.
     * <ul>
     *   <li>The returned set <b>must not be null</b>. An empty set should be returned
     *       if loading fails or if no domains are found.</li>
     *   <li>It is highly recommended to return an unmodifiable set to ensure data integrity.</li>
     *   <li>Domains within the set should be normalized, e.g., converted to lowercase
     *       and trimmed of leading/trailing whitespace.</li>
     * </ul>
     */
    Set<String> getDomains();
    // --- Static Factory Methods ---

    /**
     * Returns a loader that provides an empty set of domains.
     *
     * @return A loader instance that yields an empty set.
     */
    static DisposableDomainLoader empty() {
        // Lambda returning a pre-defined, efficient, and immutable empty set.
        return Collections::emptySet;
    }

    /**
     * Creates a loader from a pre-existing set of domains.
     * <p>
     * The provided domains will be normalized (trimmed and lower-cased).
     *
     * @param domains the set of domains to use, must not be null.
     * @return A loader instance based on the provided set. The returned domains
     * will be an unmodifiable view of a normalized version of the input set.
     */
    static DisposableDomainLoader of(Set<String> domains) {
        // java.util.Objects.requireNonNull(domains, "Domain set cannot be null.");
        final Set<String> immutableDomains = Collections.unmodifiableSet(
                domains.stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet())
        );
        return () -> immutableDomains;
    }

    /**
     * Creates a loader that reads domains from a file, with one domain per line.
     * <p>
     * This factory method handles {@link IOException} gracefully. If the file cannot be read
     * (e.g., it does not exist or permissions are denied), it will log a warning to stderr
     * (a proper logging framework should be used in a production environment) and return
     * an empty set. This prevents checked exceptions from breaking the functional nature of the interface.
     *
     * @param path the path to the domain file, must not be null.
     * @return A loader instance that loads domains from the specified file.
     */
    static DisposableDomainLoader fromFile(Path path) {
        // java.util.Objects.requireNonNull(path, "Path cannot be null.");
        return () -> {
            try (Stream<String> lines = Files.lines(path)) {
                return lines
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toSet());
            } catch (IOException e) {
                log.warn("Failed to load disposable domains from {}. Reason:{}", path, e.getMessage());
                return Collections.emptySet();
            }
        };
    }
}