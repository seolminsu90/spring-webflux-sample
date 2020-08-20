package com.webflux.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webflux.model.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class SampleController {

    @Autowired
    private ReactiveRedisOperations<String, Test> sampleOps;

    @GetMapping("/test/{id}")
    public Mono<Test> selectSamples(@PathVariable String id) {
        System.out.println("id : " + id);
        return sampleOps.opsForValue().get(id);
    }

    @GetMapping("/test")
    public Flux<Test> selectAllSamples() {
        return sampleOps.keys("*").flatMap(sampleOps.opsForValue()::get);
    }

    @PutMapping("/test/{id}")
    public Mono<Boolean> insertSamples(@PathVariable String id) {
        return sampleOps.opsForValue().set(id, new Test("babo", 35));
    }
}
