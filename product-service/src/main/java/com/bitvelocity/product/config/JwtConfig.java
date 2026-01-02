package com.bitvelocity.product.config;

import com.bit.velocity.common.security.jwt.JwtProperties;
import com.bit.velocity.common.security.jwt.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * JWT Configuration for Product Service
 * 
 * Creates JwtTokenService bean with properties from application.yml
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}")  // 15 minutes
    private long accessTokenExpiration;

    @Value("${jwt.issuer:bitvelocity}")
    private String issuer;

    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setAccessTokenExpiry(Duration.ofMillis(accessTokenExpiration));
        properties.setIssuer(issuer);
        properties.setAudience("bitvelocity-api");
        return properties;
    }

    @Bean
    public JwtTokenService jwtTokenService(JwtProperties jwtProperties) {
        return new JwtTokenService(jwtProperties);
    }
}
