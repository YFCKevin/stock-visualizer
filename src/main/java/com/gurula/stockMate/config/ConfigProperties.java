package com.gurula.stockMate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigProperties {
    @Value("${spring.data.mongodb.uri}")
    private String mongodbUri;
    @Value("${config.globalDomain}")
    private String globalDomain;
    @Value("${spring.security.oauth2.client.registration.line.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.line.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.line.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.line.user-info-uri}")
    private String userInfoUri;
    @Value("${config.picSavePath}")
    private String picSavePath;
    @Value("${config.picShowPath}")
    private String picShowPath;
    @Value("${spring.redis.host}")
    private String redisDomain;
    @Value("${spring.redis.port}")
    private int redisPort;
    @Value("${spring.redis.password}")
    private String redisPassword;
    @Value("${config.ohlcDataStorePath}")
    private String ohlcDataStorePath;

    public ConfigProperties() {
    }

    public String getPicSavePath() {
        return this.picSavePath;
    }

    public void setPicSavePath(String picSavePath) {
        this.picSavePath = picSavePath;
    }

    public String getPicShowPath() {
        return this.picShowPath;
    }

    public void setPicShowPath(String picShowPath) {
        this.picShowPath = picShowPath;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTokenUri() {
        return this.tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getUserInfoUri() {
        return this.userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    public String getMongodbUri() {
        return this.mongodbUri;
    }

    public void setMongodbUri(String mongodbUri) {
        this.mongodbUri = mongodbUri;
    }

    public String getGlobalDomain() {
        return this.globalDomain;
    }

    public void setGlobalDomain(String globalDomain) {
        this.globalDomain = globalDomain;
    }

    public String getRedisDomain() {
        return redisDomain;
    }

    public void setRedisDomain(String redisDomain) {
        this.redisDomain = redisDomain;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getOhlcDataStorePath() {
        return ohlcDataStorePath;
    }

    public void setOhlcDataStorePath(String ohlcDataStorePath) {
        this.ohlcDataStorePath = ohlcDataStorePath;
    }
}
