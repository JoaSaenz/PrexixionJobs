package com.joa.prexixion.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Habilita el programador de tareas
public class PrexixionJobsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrexixionJobsApplication.class, args);
	}

}
