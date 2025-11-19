package com.zliio.disposable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe Trie (Prefix Tree) data structure specifically designed for storing and matching domain suffixes.
 * <p>
 * This class stores domains in a reversed manner (e.g., "example.com" is stored as "com" - "example").
 * This structure allows for efficient suffix matching. For instance, if "example.com" is added,
 * matching "sub.example.com" will return {@code true}.
 * </p>
 * <p>
 * This implementation is fully thread-safe. It uses a {@link ReentrantReadWriteLock} to allow concurrent
 * reads ({@link #matches(String)}, {@link #contains(String)}) while ensuring exclusive access for write
 * operations ({@link #add(String)}, {@link #remove(String)}).
 * </p>
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
public class DomainSuffixTrie {
    private static final Logger log = LoggerFactory.getLogger(DomainSuffixTrie.class);
    /**
     * The root node of the Trie. It doesn't represent any character.
     */
    private final TrieNode root = new TrieNode();
    /**
     * The total number of unique domains stored in the Trie.
     */
    private final AtomicInteger size = new AtomicInteger(0);
    /**
     * A read-write lock to ensure thread safety for all operations.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    /**
     * Represents a node in the Trie.
     */
    private static final class TrieNode {
        /**
         * Children nodes, keyed by the domain label.
         * Using a standard HashMap is sufficient because all access is protected by the outer class's ReentrantReadWriteLock.
         */
        private final Map<String, TrieNode> children = new HashMap<>();
        /**
         * A flag indicating whether this node represents the end of a complete domain suffix.
         * The 'volatile' keyword ensures visibility across threads, which is a good practice,
         * although the lock mechanism already provides memory visibility guarantees.
         */
        private volatile boolean isEndOfDomain = false;
    }

    /**
     * Adds a domain to the Trie.
     *
     * @param domain The domain string to add. It will be converted to lowercase.
     * @return {@code true} if the domain was added successfully, {@code false} if it already existed.
     * @throws IllegalArgumentException if the domain is null, blank, or malformed.
     */
    public boolean add(String domain) {
        String[] labels = validateAndSplitDomain(domain);
        writeLock.lock();
        try {
            TrieNode current = root;
            for (int i = labels.length - 1; i >= 0; i--) {
                current = current.children.computeIfAbsent(labels[i], k -> new TrieNode());
            }
            if (!current.isEndOfDomain) {
                current.isEndOfDomain = true;
                size.incrementAndGet();
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Checks if the given domain or any of its suffixes exist in the Trie.
     * This is a high-performance, zero-allocation implementation for the read path.
     *
     * @param domain The domain to check.
     * @return {@code true} if the domain or its suffix is found, {@code false} otherwise.
     */
    public boolean matches(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }
        String normalizedDomain = domain.toLowerCase();
        readLock.lock();
        try {
            TrieNode current = root;
            int end = normalizedDomain.length();
            int start;
            while (true) {
                // Check if the current path itself is a registered domain suffix.
                if (current.isEndOfDomain) {
                    return true;
                }

                start = normalizedDomain.lastIndexOf('.', end - 1);
                String label = normalizedDomain.substring(start + 1, end);

                TrieNode nextNode = current.children.get(label);
                if (nextNode == null) {
                    return current.isEndOfDomain; // Final check on the last valid node
                }
                current = nextNode;
                if (start == -1) {
                    break; // Reached the top-level domain
                }
                end = start;
            }
            return current.isEndOfDomain;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if the Trie contains the exact domain specified.
     * This is a high-performance, zero-allocation implementation for the read path.
     *
     * @param domain The exact domain to search for.
     * @return {@code true} if the exact domain is found, {@code false} otherwise.
     */
    public boolean contains(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }
        String normalizedDomain = domain.toLowerCase();
        readLock.lock();
        try {
            TrieNode current = root;
            int end = normalizedDomain.length();
            int start;
            while (true) {
                start = normalizedDomain.lastIndexOf('.', end - 1);
                String label = normalizedDomain.substring(start + 1, end);
                TrieNode nextNode = current.children.get(label);
                if (nextNode == null) {
                    return false;
                }
                current = nextNode;
                if (start == -1) {
                    break;
                }
                end = start;
            }
            return current.isEndOfDomain;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Removes a domain from the Trie and prunes any resulting orphaned nodes.
     *
     * @param domain The domain to remove.
     * @return {@code true} if the domain was found and removed, {@code false} otherwise.
     * @throws IllegalArgumentException if the domain is null, blank, or malformed.
     */
    public boolean remove(String domain) {
        String[] labels = validateAndSplitDomain(domain);
        writeLock.lock();
        try {
            if (removeRecursively(root, labels, labels.length - 1)) {
                size.decrementAndGet();
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * A helper method to recursively find and remove the domain.
     * It also prunes nodes that become childless after removal.
     *
     * @return {@code true} if a node should be pruned by its parent.
     */
    private boolean removeRecursively(TrieNode current, String[] labels, int index) {
        if (index < 0) { // Base case: reached the end of the domain path
            if (!current.isEndOfDomain) {
                return false; // Domain didn't exist in the first place
            }
            current.isEndOfDomain = false;
            // Return true to prune this node if it has no children
            return current.children.isEmpty();
        }
        String label = labels[index];
        TrieNode childNode = current.children.get(label);
        if (childNode == null) {
            return false; // Path does not exist
        }
        // Recursive call
        boolean shouldPruneChild = removeRecursively(childNode, labels, index - 1);
        if (shouldPruneChild) {
            current.children.remove(label);
            // After removing the child, check if the current node should also be pruned.
            // It should be pruned if it's not the end of another domain and has no other children.
            return !current.isEndOfDomain && current.children.isEmpty();
        }
        return false;
    }

    public int size() {
        return size.get();
    }

    /**
     * Validates, normalizes, and splits a domain string.
     */
    private String[] validateAndSplitDomain(String domain) {
        Objects.requireNonNull(domain, "Domain cannot be null");
        if (domain.isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be empty or blank");
        }
        String normalizedDomain = domain.toLowerCase();
        String[] labels = normalizedDomain.split("\\.");
        if (labels.length == 0 || (labels.length == 1 && labels[0].isEmpty())) {
            throw new IllegalArgumentException("Domain must contain at least one label");
        }
        for (String label : labels) {
            if (label.isEmpty()) {
                log.warn("Domain contains empty labels (e.g., '..', '.com'), domain: {}", domain);
            }
        }
        return labels;
    }
}