package com.ewolff.microservice.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@SpringBootApplication
@EnableCircuitBreaker
public class OrderApp {

	public static void main(String[] args) {
		SpringApplication.run(OrderApp.class, args);
	}

}
