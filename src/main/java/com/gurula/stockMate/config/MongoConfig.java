package com.gurula.stockMate.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {
    private final ConfigProperties configProperties;

    public MongoConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    protected String getDatabaseName() {
        return "stock";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(configProperties.getMongodbUri());
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "stock");
    }

}
