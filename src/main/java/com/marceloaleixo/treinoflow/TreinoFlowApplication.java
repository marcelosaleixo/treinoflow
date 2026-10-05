package com.marceloaleixo.treinoflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TreinoFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TreinoFlowApplication.class, args);
	}

}
