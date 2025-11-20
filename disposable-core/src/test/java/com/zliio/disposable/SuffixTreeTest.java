package com.zliio.disposable;

/**
 *
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
public class SuffixTreeTest {
    public static void main(String[] args) {
        DomainSuffixTrie domainSuffixTrie = new DomainSuffixTrie();
        domainSuffixTrie.add("itsc.cc");
        domainSuffixTrie.add("google.ccm");

        System.out.println(domainSuffixTrie.matches("a-itsc.cc"));
        System.out.println(domainSuffixTrie.matches("a.itsc.cc"));
        System.out.println(domainSuffixTrie.matches("a.a.itsc.cc"));
        System.out.println(domainSuffixTrie.matches("google.ccm"));
    }
}
