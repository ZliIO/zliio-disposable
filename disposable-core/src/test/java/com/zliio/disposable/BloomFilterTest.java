package com.zliio.disposable;

/**
 * BloomFilter Test
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/14
 */
public class BloomFilterTest {
    public static void main(String[] args) {
        BloomFilter<String> bloomFilter = new BloomFilter<>(300000, 0.001);
        bloomFilter.add("itsc");
        bloomFilter.add("test");
        bloomFilter.add("bloomFilter");

        System.out.println(bloomFilter.mightContain("3213"));
        System.out.println(bloomFilter.getConfig());
    }
}
