package com.kiniot.uflex.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UflexRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UflexRestApiApplication.class, args);
    }

}
