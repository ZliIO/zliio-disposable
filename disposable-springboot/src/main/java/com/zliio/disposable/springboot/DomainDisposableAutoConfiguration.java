package com.zliio.disposable.springboot;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 *
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/16
 */
@AutoConfiguration
@EnableConfigurationProperties(DomainDisposableProperties.class)
public class DomainDisposableAutoConfiguration {
}
