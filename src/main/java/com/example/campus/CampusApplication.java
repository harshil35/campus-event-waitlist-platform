package com.example.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CampusApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusApplication.class, args);
	}

}
