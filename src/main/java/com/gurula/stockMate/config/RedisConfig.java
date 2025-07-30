package com.gurula.stockMate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.Optional;

@Configuration
public class RedisConfig {

    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;

    public RedisConfig(ConfigProperties configProperties, ObjectMapper objectMapper) {
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * @Description: 防止redis入庫序列化亂碼的問題
     * @return     返回型別
     */
    @Bean(name="redis_db2_redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());//key序列化
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));  //value序列化
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(configProperties.getRedisDomain(), configProperties.getRedisPort());
        configuration.setDatabase(2);
        Optional.ofNullable(configProperties.getRedisPassword()).ifPresent(password->{
            RedisPassword redisPassword = RedisPassword.of(password);
            configuration.setPassword(redisPassword);
        });
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        objectMapper.registerModule(new JavaTimeModule()); // 支援 Java 8 日期
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 使用 GenericJackson2JsonRedisSerializer 來避免序列化錯誤
        RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues(); // 可選：不快取 null 值
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration())
                .build();
    }
}
