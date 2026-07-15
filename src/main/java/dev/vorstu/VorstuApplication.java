package dev.vorstu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VorstuApplication {
	public static void main(String[] args) {
		SpringApplication.run(VorstuApplication.class, args);
	}
}