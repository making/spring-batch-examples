package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBatchExamplesApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(SpringBatchExamplesApplication.class, args);
		int exitCode = SpringApplication.exit(context);
		System.exit(exitCode);
	}

}
