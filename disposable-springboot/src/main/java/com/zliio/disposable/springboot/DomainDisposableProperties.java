package com.zliio.disposable.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the disposable email domain checker.
 * <p>
 * This class maps properties prefixed with {@code "disposable"} from application configuration files
 * (e.g., application.yml) to configure the behavior of the domain checking service.
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
@Configuration
@ConfigurationProperties(prefix = "disposable")
public class DomainDisposableProperties {
    /**
     * The algorithm type used for checking disposable domains.
     * Supported values are "BloomFilter" or "SuffixTrie". Defaults to "SuffixTrie".
     */
    private String algorithm = "SuffixTrie";
    /**
     * Configuration for the disposable domain list loader.
     * Specifies how the domain list is loaded into memory.
     */
    private DomainLoaderConfig loader = new DomainLoaderConfig();
    /**
     * A nested configuration class for the domain list loader.
     * It groups properties related to loading the domain data, such as the source type and path.
     */
    public static class DomainLoaderConfig {
        /**
         * The type of loader to use for the domain list.
         * Supported values are "built-in" (uses the default list packaged with the application)
         * or "http" (fetches the list from a remote URL). Defaults to "built-in".
         */
        private String type = "built-in";
        /**
         * The specific path or URL for the domain list. This is optional.
         * <p>
         * If the {@code type} is "http", this should be a valid URL pointing to a plain text file
         * where each line is a domain.
         * If using a custom file-based loader, this could be a classpath resource path (e.g., "classpath:/custom-domains.txt").
         * If not specified, the default built-in list is used.
         */
        private String path;
        // --- Getters and Setters for DomainLoaderConfig ---
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }
    }
    // --- Getters and Setters for DomainDisposableProperties ---
    public String getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    public DomainLoaderConfig getLoader() {
        return loader;
    }
    public void setLoader(DomainLoaderConfig loader) {
        this.loader = loader;
    }
}