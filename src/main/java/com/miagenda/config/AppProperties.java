package com.miagenda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String secretKey, long accessTokenExpireMinutes) {
}
