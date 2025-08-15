package com.nimbusstore.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StorageNodeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StorageNodeApiApplication.class, args);
	}

}
