package com.zliio.disposable.loader;

import com.zliio.disposable.DisposableDomainLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of {@link DisposableDomainLoader} that loads a set of disposable email domains
 * from a remote text file over HTTP.
 * <p>
 * Each line in the text file is expected to be a single domain name. This class is designed
 * to be robust, handling potential network issues gracefully.
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
public class HttpTxtDomainLoader implements DisposableDomainLoader {
    /**
     * The default URL pointing to a publicly available list of disposable email domains.
     */
    public static final String DEFAULT_DOMAINS_URL = "https://disposable.github.io/disposable-email-domains/domains.txt";
    private static final int CONNECT_TIMEOUT_MS = 5000; // 5 seconds
    private static final int READ_TIMEOUT_MS = 10000; // 10 seconds
    private final String url;

    /**
     * Constructs a domain loader that uses the {@link #DEFAULT_DOMAINS_URL}.
     */
    public HttpTxtDomainLoader() {
        this(DEFAULT_DOMAINS_URL);
    }

    /**
     * Constructs a domain loader with a specific URL.
     *
     * @param url the URL of the .txt file to fetch domains from. Must not be null.
     * @throws NullPointerException if the url is null.
     */
    public HttpTxtDomainLoader(String url) {
        this.url = Objects.requireNonNull(url, "URL cannot be null.");
    }

    /**
     * Fetches the set of disposable domains from the configured URL.
     * <p>
     * This method performs an HTTP GET request to the specified URL. It reads the response body
     * line by line, treating each line as a domain. In case of any network error or if the
     * server returns a non-successful response code, it will log the error and return an
     * empty set.
     *
     * @return a {@link Set} of domain strings. Returns an empty set if the domains cannot be
     * fetched or if the source is empty. The returned set is unmodifiable.
     */
    @Override
    public Set<String> getDomains() {
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(this.url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            // Set robust timeouts to prevent the application from hanging on network issues.
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // Success
                // Use try-with-resources for automatic resource management.
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    // Use Stream API for a more concise and functional approach.
                    return in.lines()
                            .filter(line -> line != null && !line.trim().isEmpty())
                            .collect(Collectors.toSet());
                }
            } else {
                log.warn("Failed to fetch domains. HTTP Response Code: {} from URL: {}", responseCode, this.url);
                return Collections.emptySet();
            }
        } catch (IOException e) {
            log.error("An IOException occurred while fetching domains from URL: {}", this.url, e);
            return Collections.emptySet();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
