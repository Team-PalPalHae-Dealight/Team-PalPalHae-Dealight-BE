package com.palpal.dealightbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DealightBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DealightBeApplication.class, args);
	}

}
