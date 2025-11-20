package com.zliio.disposable;

import java.util.BitSet;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

/**
 * A high-performance Bloom filter implementation for probabilistic set membership testing.
 * Supports automatic parameter calculation and various optimization strategies.
 * <p>
 * The Bloom filter provides space-efficient storage with configurable false positive probability.
 * It uses a bit array and multiple hash functions to represent set membership probabilistically.
 *
 * @param <T> the type of elements maintained by this Bloom filter
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/13
 */
public class BloomFilter<T> {
    /**
     * Maximum allowed number of hash functions to prevent performance degradation
     */
    private static final int MAX_HASH_FUNCTIONS = 100;

    /**
     * Saturation threshold ratio (when bit usage exceeds this ratio, filter is considered saturated)
     */
    private static final double SATURATION_THRESHOLD = 0.75;

    /**
     * Precomputed mathematical constants for performance
     */
    private static final double LN_2 = Math.log(2);
    /**
     * LN_2 * LN_2.
     */
    private static final double SQUARE_LN_2 = LN_2 * LN_2;

    /**
     * The size of the bit array (number of bits)
     */
    private final int size;

    /**
     * The number of hash functions to use
     */
    private final int hashFunctions;

    /**
     * The bit array used to store element presence
     */
    private final BitSet bitSet;

    /**
     * Approximate count of inserted elements (thread-safe)
     */
    private final LongAdder elementCount;

    /**
     * The target false positive probability
     */
    private final double falsePositiveProbability;

    /**
     * Hash strategy implementation
     */
    private final HashStrategy hashStrategy;

    // ==================== CONSTRUCTORS ====================

    /**
     * Constructs a Bloom filter that automatically calculates optimal parameters
     * based on expected number of insertions and target false positive probability.
     *
     * @param expectedInsertions       the expected number of elements to be inserted
     * @param falsePositiveProbability the desired false positive probability (0.0 to 1.0, exclusive)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public BloomFilter(long expectedInsertions, double falsePositiveProbability) {
        this(expectedInsertions, falsePositiveProbability, true);
    }

    /**
     * Private constructor - unified entry point for all public constructors
     *
     * @param expectedInsertions         the expected number of elements to be inserted
     * @param falsePositiveProbability   the target false positive probability
     * @param autoCalculateHashFunctions whether to automatically calculate optimal hash functions
     */
    private BloomFilter(long expectedInsertions, double falsePositiveProbability, boolean autoCalculateHashFunctions) {
        int size = calculateOptimalSize(expectedInsertions, falsePositiveProbability);
        validateParameters(size, falsePositiveProbability);
        this.size = size;
        this.falsePositiveProbability = falsePositiveProbability;
        this.bitSet = new BitSet(size);
        this.elementCount = new LongAdder();
        if (autoCalculateHashFunctions) {
            this.hashFunctions = calculateOptimalHashFunctions(size, expectedInsertions);
        } else {
            this.hashFunctions = calculateHashFunctionsSimplified(falsePositiveProbability);
        }
        this.hashStrategy = new EnhancedHashStrategy();
        validateConfiguration();
    }

    // ==================== PUBLIC API ====================

    /**
     * Adds an element to the Bloom filter.
     * Sets the corresponding bits in the bit array based on hash function results.
     *
     * @param item the element to be added to the Bloom filter (cannot be null)
     * @throws IllegalArgumentException if item is null
     */
    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        int[] hashes = hashStrategy.hashes(item, hashFunctions, size);
        for (int hash : hashes) {
            bitSet.set(hash);
        }
        elementCount.increment();
    }

    /**
     * Adds multiple elements to the Bloom filter in batch (performance optimized).
     *
     * @param items the elements to be added to the Bloom filter
     * @throws IllegalArgumentException if any item is null
     */
    @SafeVarargs
    public final void addAll(T... items) {
        for (T item : items) {
            add(item);
        }
    }

    /**
     * Checks if the element might be contained in the Bloom filter.
     * <p>
     * Returns true if all corresponding bits are set (may include false positives).
     * Returns false if any corresponding bit is not set (definitely not in the set).
     * This property makes Bloom filters suitable for cases where false positives are acceptable
     * but false negatives are not.
     *
     * @param item the element whose presence is to be tested
     * @return false if the element is definitely not in the set,
     * true if the element might be in the set (with possible false positive)
     */
    public boolean mightContain(T item) {
        if (item == null) {
            return false;
        }

        int[] hashes = hashStrategy.hashes(item, hashFunctions, size);
        for (int hash : hashes) {
            if (!bitSet.get(hash)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the approximate number of elements inserted into the Bloom filter.
     * This count is maintained approximately for performance reasons.
     *
     * @return the approximate count of inserted elements
     */
    public long getApproximateElementCount() {
        return elementCount.longValue();
    }

    /**
     * Estimates the current actual false positive rate based on the current number of elements.
     * The actual false positive rate may differ from the target rate as more elements are added.
     *
     * @return the estimated current false positive rate (0.0 to 1.0)
     */
    public double getActualFalsePositiveRate() {
        long n = getApproximateElementCount();
        if (n == 0) return 0.0;

        // Actual FPR formula: (1 - e^(-k * n / m)) ^ k
        double exponent = -hashFunctions * n / (double) size;
        return Math.pow(1 - Math.exp(exponent), hashFunctions);
    }

    /**
     * Returns the current configuration and statistics of the Bloom filter.
     *
     * @return a BloomFilterConfig object containing current filter state
     */
    public BloomFilterConfig getConfig() {
        return new BloomFilterConfig(size, hashFunctions, getExpectedInsertions(),
                falsePositiveProbability, getActualFalsePositiveRate(),
                getBitUsageRatio(), getApproximateElementCount());
    }

    /**
     * Clears the Bloom filter, removing all elements and resetting counters.
     * The filter can be reused after clearing.
     */
    public void clear() {
        bitSet.clear();
        elementCount.reset();
    }

    /**
     * Returns the ratio of bits that are set in the bit array.
     * A high usage ratio may indicate that the filter is approaching saturation.
     *
     * @return the ratio of set bits (0.0 to 1.0)
     */
    public double getBitUsageRatio() {
        return bitSet.cardinality() / (double) size;
    }

    /**
     * Checks if the Bloom filter is likely saturated.
     * A saturated filter will have significantly higher false positive rates.
     * Saturation threshold is set at 75% bit usage.
     *
     * @return true if the filter is likely saturated, false otherwise
     */
    public boolean isLikelySaturated() {
        return getBitUsageRatio() > SATURATION_THRESHOLD;
    }

    /**
     * Returns the size of the bit array in bits.
     *
     * @return the size of the bit array
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the number of hash functions used by this Bloom filter.
     *
     * @return the number of hash functions
     */
    public int getHashFunctions() {
        return hashFunctions;
    }

    /**
     * Returns the target false positive probability for this Bloom filter.
     *
     * @return the target false positive probability
     */
    public double getFalsePositiveProbability() {
        return falsePositiveProbability;
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Calculates the optimal size of the bit array based on expected insertions and false positive rate.
     *
     * @param expectedInsertions       the expected number of elements
     * @param falsePositiveProbability the target false positive probability
     * @return the optimal size in bits
     */
    private static int calculateOptimalSize(long expectedInsertions, double falsePositiveProbability) {
        validateParameters((int) Math.min(expectedInsertions, Integer.MAX_VALUE), falsePositiveProbability);

        // Optimal size formula: m = - (n * ln(ε)) / (ln(2))²
        double numerator = -expectedInsertions * Math.log(falsePositiveProbability);
        long optimalSize = (long) Math.ceil(numerator / SQUARE_LN_2);

        // Ensure within Integer bounds and at least 1
        return (int) Math.max(1, Math.min(optimalSize, Integer.MAX_VALUE - 8));
    }

    /**
     * Calculates the optimal number of hash functions based on bit array size and expected insertions.
     *
     * @param bitSize            the size of the bit array
     * @param expectedInsertions the expected number of elements
     * @return the optimal number of hash functions
     */
    private static int calculateOptimalHashFunctions(long bitSize, long expectedInsertions) {
        // Optimal k formula: k = (m / n) * ln(2)
        int k = (int) Math.ceil((bitSize / (double) expectedInsertions) * LN_2);
        return Math.max(1, k);
    }

    /**
     * Calculates hash functions using the simplified formula (for backward compatibility).
     *
     * @param falsePositiveProbability the false positive probability
     * @return the number of hash functions
     */
    private static int calculateHashFunctionsSimplified(double falsePositiveProbability) {
        return (int) Math.ceil(-Math.log(falsePositiveProbability) / Math.log(2));
    }

    /**
     * Estimates the expected number of insertions based on current configuration.
     *
     * @return the estimated expected insertions
     */
    private long getExpectedInsertions() {
        return Math.max(1, (long) (size * LN_2 / hashFunctions));
    }

    /**
     * Validates constructor parameters.
     *
     * @param size                     the size parameter to validate
     * @param falsePositiveProbability the probability parameter to validate
     * @throws IllegalArgumentException if parameters are invalid
     */
    private static void validateParameters(int size, double falsePositiveProbability) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        if (falsePositiveProbability <= 0.0 || falsePositiveProbability >= 1.0) {
            throw new IllegalArgumentException(
                    "False positive probability must be between 0 and 1 (exclusive): " + falsePositiveProbability);
        }
    }

    /**
     * Validates the Bloom filter configuration after construction.
     *
     * @throws IllegalStateException if the configuration is invalid
     */
    private void validateConfiguration() {
        if (hashFunctions <= 0) {
            throw new IllegalStateException("Hash functions count must be positive: " + hashFunctions);
        }
        if (hashFunctions > MAX_HASH_FUNCTIONS) {
            throw new IllegalStateException("Too many hash functions: " + hashFunctions +
                    ". Consider increasing size or false positive rate");
        }
    }

    // ==================== INNER CLASSES AND INTERFACES ====================

    /**
     * Interface for hash generation strategies.
     */
    private interface HashStrategy {
        /**
         * Generates multiple hash values for an item.
         *
         * @param item      the item to hash
         * @param numHashes the number of hash values to generate
         * @param size      the size for modulo operation
         * @return an array of hash values
         */
        int[] hashes(Object item, int numHashes, int size);
    }

    /**
     * Enhanced hash strategy using double hashing for better distribution.
     */
    private static class EnhancedHashStrategy implements HashStrategy {
        /**
         * Prime number for second hash function
         */
        private static final int HASH_SEED = 0x5bd1e995;

        @Override
        public int[] hashes(Object item, int numHashes, int size) {
            int[] hashes = new int[numHashes];

            // Use double hashing to generate multiple hash values
            int h1 = Objects.hashCode(item);
            int h2 = (h1 ^ HASH_SEED); // Second hash seed

            // Ensure h2 is odd for better distribution in double hashing
            if (h2 % 2 == 0) {
                h2 += 1;
            }

            for (int i = 0; i < numHashes; i++) {
                int combinedHash = h1 + i * h2;
                // Ensure non-negative and within bit array bounds
                hashes[i] = Math.abs(combinedHash % size);
            }

            return hashes;
        }
    }

    /**
     * Configuration and statistics container for Bloom filter.
     */
    public static final class BloomFilterConfig {
        private final int bitSize;
        private final int hashFunctions;
        private final long expectedElements;
        private final double targetFalsePositiveRate;
        private final double actualFalsePositiveRate;
        private final double bitUsageRatio;
        private final long currentElementCount;

        public BloomFilterConfig(int bitSize, int hashFunctions, long expectedElements,
                                 double targetFalsePositiveRate, double actualFalsePositiveRate,
                                 double bitUsageRatio, long currentElementCount) {
            this.bitSize = bitSize;
            this.hashFunctions = hashFunctions;
            this.expectedElements = expectedElements;
            this.targetFalsePositiveRate = targetFalsePositiveRate;
            this.actualFalsePositiveRate = actualFalsePositiveRate;
            this.bitUsageRatio = bitUsageRatio;
            this.currentElementCount = currentElementCount;
        }

        // ==================== GETTERS ====================

        public int getBitSize() {
            return bitSize;
        }

        public int getHashFunctions() {
            return hashFunctions;
        }

        public long getExpectedElements() {
            return expectedElements;
        }

        public double getTargetFalsePositiveRate() {
            return targetFalsePositiveRate;
        }

        public double getActualFalsePositiveRate() {
            return actualFalsePositiveRate;
        }

        public double getBitUsageRatio() {
            return bitUsageRatio;
        }

        public long getCurrentElementCount() {
            return currentElementCount;
        }

        public long getByteSize() {
            return (bitSize + 7) / 8;
        }

        public double getMemoryKB() {
            return getByteSize() / 1024.0;
        }

        public double getMemoryMB() {
            return getMemoryKB() / 1024.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "BloomFilter Configuration: " +
                            "Bits=%,d (%.2f MB), Hash Functions=%d, Expected Elements=%,d, " +
                            "Target FPR=%.4f%%, Actual FPR=%.4f%%, Bit Usage=%.2f%%, Current Elements=%,d",
                    bitSize, getMemoryMB(), hashFunctions, expectedElements,
                    targetFalsePositiveRate * 100, actualFalsePositiveRate * 100,
                    bitUsageRatio * 100, currentElementCount
            );
        }
    }
}