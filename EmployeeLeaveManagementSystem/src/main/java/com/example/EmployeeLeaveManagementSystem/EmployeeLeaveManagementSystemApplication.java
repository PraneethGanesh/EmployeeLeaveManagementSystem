package com.example.EmployeeLeaveManagementSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmployeeLeaveManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeLeaveManagementSystemApplication.class, args);
	}

}
