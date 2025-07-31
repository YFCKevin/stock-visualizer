package com.gurula.stockMate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@EnableCaching
@EnableTransactionManagement
@SpringBootApplication
public class StockMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockMateApplication.class, args);
	}

}
