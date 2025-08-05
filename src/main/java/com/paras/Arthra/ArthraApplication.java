package com.paras.Arthra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ArthraApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArthraApplication.class, args);
	}

}
