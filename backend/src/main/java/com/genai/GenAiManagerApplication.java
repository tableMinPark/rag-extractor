package com.genai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.genai"})
public class GenAiManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenAiManagerApplication.class, args);
    }
}
