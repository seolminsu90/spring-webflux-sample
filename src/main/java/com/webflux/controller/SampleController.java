package com.webflux.controller;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData; //★

import java.util.Arrays;
import java.util.List;

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
import reactor.core.scheduler.Schedulers;

@RestController
public class SampleController {

    // -------------------------------------------- Webclient 초기화

    private final WebClient webClient;

    public SampleController(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8080/remote").build();
    }

    // -------------------------------------------- Rest Request 예시

    // https://luvstudy.tistory.com/100

    @GetMapping("/test")
    public Mono<Test> test(String msg) {
        return waterfallRequest();
    }

    // Post Call Example
    private Mono<Test> POST_greeting(int apiNo){
        return webClient.post()
                .uri("/test")
                .body(fromFormData("name", "seol"))
                .retrieve()
                .bodyToMono(Test.class);
    }

    // Get Call Example
    private Mono<Test> GET_greeting(int apiNo){
        return webClient.get()
                .uri("/test" +apiNo+ "/{msg}", "   hihi")
                .retrieve()
                .bodyToMono(Test.class);
    }

    // -------------------------------------------- WebClient 활용

    // Chain Waterfall Example
    private Mono<Test> waterfallRequest(){
        // 하나의 waterfall 예시
        Mono<Test> request1 = GET_greeting(1);
        Mono<Test> value = request1.flatMap(msg -> {
            return GET_greeting(2);
        });
        // * 1:1 .map,  1:n .flatMap (not concurrency) .concatMap or .flatMapSequential (concurrency)

        // 다수의 waterfall 예시
        GET_greeting(1)
            .flatMap(msg -> {System.out.println(msg.getName()); return GET_greeting(2);})
            .flatMap(msg -> {System.out.println(msg.getName()); return GET_greeting(3);})
            .flatMap(msg -> {System.out.println(msg.getName()); return GET_greeting(4);})
            .flatMap(msg -> {System.out.println(msg.getName()); return GET_greeting(5);})
            .subscribe();

        return value;
    }

    // Other Type Or Service Multiple
    private Mono<Object> multipleCallTest1(){
        Mono<Test> test1 = GET_greeting(1).subscribeOn(Schedulers.elastic());
        Mono<Test> test2 = GET_greeting(2).subscribeOn(Schedulers.elastic());

        return Mono.zip(test1, test2, (t1, t2) -> t1);
    }

    // Other Service Multiple
    private Flux<Test> multipleCallTest2() {
        return Flux.merge(GET_greeting(1), GET_greeting(2))
            .parallel()
            .runOn(Schedulers.elastic())
            .ordered((u1, u2) -> u2.getAge() - u1.getAge());
    }

    // Same Service Multiple
    private Flux<Test> multipleCallTest3() {
        List<Integer> list = Arrays.asList(1,2,3,4,5);
        return Flux.fromIterable(list)
            .parallel()
            .runOn(Schedulers.elastic())
            .flatMap(this::GET_greeting)
            .ordered((u1, u2) -> u2.getAge() - u1.getAge()); // 각 Publisher가 병렬 수행되서 Ordered 해줘야 Flux로 변환됨
    }

    // -------------------------------------------- 공통 Response객체 사용 시
    @GetMapping("/example/{userId}")
    public Mono<ResponseEntity<Test>> get(@PathVariable Long userId) {
        Mono<Test> testMono = Mono.just(new Test("test", 1));
        return testMono.map((user) -> { // Mono<Test> -> Map 이용해서 변환
            if (user.isAdult()) {
                return ResponseEntity.ok().header("X-User-Adult", "true").build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        });
    }

    // -------------------------------------------- Reactive 테스트용 ReactiveRedis 관련

    @Autowired
    private ReactiveRedisOperations<String, Test> sampleOps;

    @GetMapping("/redis/test/{id}")
    public Mono<Test> selectSamples(@PathVariable String id) {
        return sampleOps.opsForValue().get(id);
    }

    @GetMapping("/redis/test")
    public Flux<Test> selectAllSamples() {
        return sampleOps.keys("*")
                    .flatMap(sampleOps.opsForValue()::get);
    }

    @PutMapping("/redis/test/{id}")
    public Mono<Boolean> insertSamples(@PathVariable String id) {
        return sampleOps.opsForValue().set(id, new Test("babo", 35));
    }
}
