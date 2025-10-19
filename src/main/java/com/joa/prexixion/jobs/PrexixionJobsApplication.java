package com.joa.prexixion.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync // 🔹 Permite ejecutar tareas asíncronas (como la sincronización manual)
@EnableScheduling // Habilita el programador de tareas
public class PrexixionJobsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrexixionJobsApplication.class, args);
	}

}
