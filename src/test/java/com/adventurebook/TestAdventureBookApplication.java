package com.adventurebook;

import org.springframework.boot.SpringApplication;

public class TestAdventureBookApplication {

	public static void main(String[] args) {
		SpringApplication.from(AdventureBookApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
