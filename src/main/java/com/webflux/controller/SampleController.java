package com.webflux.controller;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData; //★

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.webflux.model.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class SampleController {

    // Using Webclint
    private final WebClient webClient;

    public SampleController(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://대상주소:9999").build();
    }

    @GetMapping("/test")
    public Mono<String> selectGreeting(String msg) {
        // 기타 상황에 유용한 Operator 너무많다.
        // https://luvstudy.tistory.com/100

        // Post Example
        webClient.post()
                 .uri("/sample")
                 .body(fromFormData("name", "wonwoo"))
                 .retrieve()
                 .bodyToMono(String.class);

        // Get Example
        return webClient.get()
                        .uri("/test/{msg}", msg)
                        .retrieve()
                        .bodyToMono(String.class);
    }

    // Using ReactiveRedis
    @Autowired
    private ReactiveRedisOperations<String, Test> sampleOps;

    @GetMapping("/test/{id}")
    public Mono<Test> selectSamples(@PathVariable String id) {
        return sampleOps.opsForValue().get(id);
    }

    @GetMapping("/test")
    public Flux<Test> selectAllSamples() {
        return sampleOps.keys("*")
                    .flatMap(sampleOps.opsForValue()::get);
    }

    @PutMapping("/test/{id}")
    public Mono<Boolean> insertSamples(@PathVariable String id) {
        return sampleOps.opsForValue().set(id, new Test("babo", 35));
    }
}
