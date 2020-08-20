package com.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringWebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebfluxApplication.class, args);
    }
}
