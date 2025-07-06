package com.driver.location_saver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LocationSaverApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocationSaverApplication.class, args);
	}

}
