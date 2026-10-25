package com.normcat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NormCatApplication {

    public static void main(String[] args) {
        SpringApplication.run(NormCatApplication.class, args);
    }
}
