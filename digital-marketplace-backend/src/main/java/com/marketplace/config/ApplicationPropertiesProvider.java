package com.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "marketplace")
public class ApplicationPropertiesProvider {

    // Application Configuration
    private String name = "Digital Marketplace";

    // Frontend Configuration
    private String frontendUrl = "http://localhost:3000";

    // Image Configuration
    private long maxImageSize = 52428800L; // 50MB default
    private String allowedImageTypes = "jpg,jpeg,png,webp,tiff";

    // Transaction Configuration
    private double commissionRate = 0.10; // 10%
    private double processingFeeRate = 0.029; // 2.9%

    // JWT Configuration
    private Jwt jwt = new Jwt();

    // Mail Configuration
    private Mail mail = new Mail();

    // Nested classes for grouped properties
    public static class Jwt {
        private String secret;
        private long expiration = 86400000L; // 24 hours default
        private long refreshExpiration = 604800000L; // 7 days default

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }

        public long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }

    public static class Mail {
        private String from = "noreply@marketplace.com";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public long getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(long maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public String getAllowedImageTypes() {
        return allowedImageTypes;
    }

    public void setAllowedImageTypes(String allowedImageTypes) {
        this.allowedImageTypes = allowedImageTypes;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public double getProcessingFeeRate() {
        return processingFeeRate;
    }

    public void setProcessingFeeRate(double processingFeeRate) {
        this.processingFeeRate = processingFeeRate;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }
}
