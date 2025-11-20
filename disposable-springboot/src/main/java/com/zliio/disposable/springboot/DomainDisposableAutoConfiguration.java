package com.zliio.disposable.springboot;

import com.zliio.disposable.Disposable;
import com.zliio.disposable.DisposableDomainLoader;
import com.zliio.disposable.core.BloomFilterDisposable;
import com.zliio.disposable.core.SuffixTrieDisposable;
import com.zliio.disposable.loader.BuiltinDomainLoader;
import com.zliio.disposable.loader.HttpTxtDomainLoader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Auto-Configuration for the Disposable Email domain checking service.
 * <p>
 * This configuration class automatically sets up the necessary beans for domain checking
 * based on the properties defined in {@link DomainDisposableProperties}. It allows for
 * configuring different domain loading strategies (e.g., builtin, http) and different
 * checking algorithms (e.g., SuffixTrie, BloomFilter).
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
@AutoConfiguration
@EnableConfigurationProperties(DomainDisposableProperties.class)
public class DomainDisposableAutoConfiguration {
    /**
     * Constant for the Suffix Trie algorithm identifier.
     */
    private static final String ALGORITHM_SUFFIX_TRIE = "SuffixTrie";
    /**
     * Constant for the Bloom Filter algorithm identifier.
     */
    private static final String ALGORITHM_BLOOM_FILTER = "BloomFilter";
    /**
     * Constant for the built-in domain loader type.
     */
    private static final String DOMAIN_LOADER_TYPE_BUILTIN = "builtin";
    /**
     * Constant for the HTTP TXT domain loader type.
     */
    private static final String DOMAIN_LOADER_TYPE_HTTP = "http";

    /**
     * Configuration for creating the {@link DisposableDomainLoader} bean.
     * This inner class groups all beans related to domain loading.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(DisposableDomainLoader.class)
    static class DomainLoaderConfiguration {
        /**
         * Creates a {@link BuiltinDomainLoader} bean if the loader type is configured as 'builtin'
         * or if the property is not specified at all (making it the default).
         * <p>
         * This bean will not be created if a bean of type {@link DisposableDomainLoader} already exists in the context.
         *
         * @param properties The configuration properties for disposable domains.
         * @return A {@link BuiltinDomainLoader} instance.
         */
        @Bean
        @ConditionalOnProperty(prefix = "domain.disposable.loader", name = "type", havingValue = DOMAIN_LOADER_TYPE_BUILTIN, matchIfMissing = true)
        public DisposableDomainLoader builtinDisposableDomainLoader(DomainDisposableProperties properties) {
            DomainDisposableProperties.DomainLoaderConfig loaderProps = properties.getLoader();
            // Use user-defined path if provided, otherwise use the default constructor.
            if (loaderProps != null && StringUtils.hasText(loaderProps.getPath())) {
                return new BuiltinDomainLoader(loaderProps.getPath());
            }
            return new BuiltinDomainLoader();
        }

        /**
         * Creates a {@link HttpTxtDomainLoader} bean if the loader type is configured as 'http'.
         * <p>
         * This bean will not be created if a bean of type {@link DisposableDomainLoader} already exists in the context.
         *
         * @param properties The configuration properties for disposable domains.
         * @return A {@link HttpTxtDomainLoader} instance.
         */
        @Bean
        @ConditionalOnProperty(prefix = "domain.disposable.loader", name = "type", havingValue = DOMAIN_LOADER_TYPE_HTTP)
        public DisposableDomainLoader httpDisposableDomainLoader(DomainDisposableProperties properties) {
            DomainDisposableProperties.DomainLoaderConfig loaderProps = properties.getLoader();
            // Use user-defined path if provided and valid, otherwise use the default constructor.
            if (loaderProps != null && StringUtils.hasText(loaderProps.getPath()) && loaderProps.getPath().startsWith("http")) {
                return new HttpTxtDomainLoader(loaderProps.getPath());
            }
            return new HttpTxtDomainLoader();
        }
    }

    /**
     * Configuration for creating the {@link Disposable} checker bean.
     * <p>
     * This configuration is only activated if a {@link DisposableDomainLoader} bean is present in the Spring context.
     * This ensures that the checker is not created without a valid domain source.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(Disposable.class)
    @AutoConfigureAfter(DomainLoaderConfiguration.class)
    static class DisposableCheckerConfiguration {
        /**
         * Creates a {@link SuffixTrieDisposable} bean if the algorithm is configured as 'SuffixTrie'
         * or if the property is not specified (making it the default).
         * <p>
         * This bean will not be created if a bean of type {@link Disposable} already exists in the context.
         *
         * @param domainLoader The {@link DisposableDomainLoader} bean to be used for loading domains.
         *                     This is automatically injected by Spring from the context.
         * @return A {@link SuffixTrieDisposable} instance.
         */
        @Bean
        @ConditionalOnProperty(prefix = "domain.disposable", name = "algorithm", havingValue = ALGORITHM_SUFFIX_TRIE, matchIfMissing = true)
        public Disposable suffixTrieDisposable(DisposableDomainLoader domainLoader) {
            return new SuffixTrieDisposable(domainLoader);
        }

        /**
         * Creates a {@link BloomFilterDisposable} bean if the algorithm is configured as 'BloomFilter'.
         * <p>
         * This bean will not be created if a bean of type {@link Disposable} already exists in the context.
         *
         * @param domainLoader The {@link DisposableDomainLoader} bean to be used for loading domains.
         *                     This is automatically injected by Spring from the context.
         * @return A {@link BloomFilterDisposable} instance.
         */
        @Bean
        @ConditionalOnProperty(prefix = "domain.disposable", name = "algorithm", havingValue = ALGORITHM_BLOOM_FILTER)
        public Disposable bloomFilterDisposable(DisposableDomainLoader domainLoader) {
            return new BloomFilterDisposable(domainLoader);
        }
    }
}