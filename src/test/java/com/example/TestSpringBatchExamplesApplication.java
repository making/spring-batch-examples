package com.example;

import org.springframework.boot.SpringApplication;

public class TestSpringBatchExamplesApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringBatchExamplesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
