package com.webflux.rest;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData; //★

import java.util.Arrays;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.webflux.common.model.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

@RestController
public class WebClientSample {

    private final WebClient webClient;

    public WebClientSample(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8080/remote").build();
    }

    // https://luvstudy.tistory.com/100

    @GetMapping("/test")
    public void test(String msg) {
        waterfallRequest();
    }

    // Post Call Example
    private Mono<Test> POST_greeting(int apiNo) {
        return webClient.post().uri("/test").body(fromFormData("name", "seol")).retrieve().bodyToMono(Test.class);
    }

    // Get Call Example
    private Mono<Test> GET_greeting(int apiNo) {
        return webClient.get().uri("/test" + apiNo + "/{msg}", "   hihi").retrieve().bodyToMono(Test.class);
    }

    // webClient Exception Handling
    private void webClientExceptionExample() {
        webClient.mutate() // Builder 재활용해서 설정만 다르게 해서 쓰는방식
                 .baseUrl("https://some.com")
                 .build()
                 .get()
                 .uri("/resource")
                 .accept(MediaType.APPLICATION_JSON)
                 .retrieve() // 데이터를 받는 방식 exchange()는 Memory leak으로 인해 사용을 권고하지 않음.
                 .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                         clientResponse -> clientResponse.bodyToMono(String.class)
                                                         .map(body -> new RuntimeException(body)))
                 .bodyToMono(String.class);
    }

    // -------------------------------------------- WebClient 활용

    // 하향식 수행
    private void waterfallRequest() {
        GET_greeting(1).subscribe(test -> {
            System.out.println(test.getName() + ":oneSubscribe");
        });

        // * 1:1 .map, 1:n .flatMap (not concurrency) .concatMap or .flatMapSequential
        // (concurrency)
        // 하향식으로 데이터를 받아가며 수행
        GET_greeting(1).flatMap(msg -> GET_greeting(2))
                       .flatMap(msg -> GET_greeting(3))
                       .flatMap(msg -> GET_greeting(4))
                       .flatMap(msg -> GET_greeting(5))
                       .subscribe();

        // then,, thenMany 완료후 다른 mono/flux를 연결해준다.
        GET_greeting(1).then(GET_greeting(2)).subscribe(a -> System.out.println(a.getName()));

        Flux.just("1,2,3").thenMany(Flux.just("4,5")).subscribe(a -> System.out.println(a));

        zipWhentTest();
    }

    private Mono<Void> zipWhentTest() {
        Mono.just("hello")
            .zipWhen(it -> Mono.just(it + " world"))
            .subscribe((Tuple2<String, String> it) -> System.out.println(it.getT1() + ", " + it.getT2()));
        return Mono.empty();
    }

    // 서비스 병렬 수행
    private Mono<Object> multipleCallTest1() {
        Mono<Test> test1 = GET_greeting(1).subscribeOn(Schedulers.elastic());
        Mono<Test> test2 = GET_greeting(2).subscribeOn(Schedulers.elastic());

        return Mono.zip(test1, test2, (t1, t2) -> t1);
    }

    // 서비스 병렬 수행
    private Flux<Test> multipleCallTest2() {
        return Flux.merge(GET_greeting(1), GET_greeting(2))
                   .parallel()
                   .runOn(Schedulers.elastic())
                   .ordered((u1, u2) -> u2.getAge() - u1.getAge()); // 각 Publisher가 병렬 수행되서 순서가 없어서 정렬 후 Flux로 변환됨
    }

    // 서비스 반복 수행
    private Flux<Test> multipleCallTest3() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        return Flux.fromIterable(list)
                   .parallel()
                   .runOn(Schedulers.elastic())
                   .flatMap(this::GET_greeting)
                   .ordered((u1, u2) -> u2.getAge() - u1.getAge()); // 각 Publisher가 병렬 수행되서 순서가 없어서 정렬 후 Flux로 변환됨
    }

    public void castExample() {
        // Type cast 오류시 오류로
        Mono.just(1)
            .cast(String.class)
            .subscribe(System.out::println, (e) -> System.out.println("error"), () -> System.out.println("complete"));

        // Type cast 오류가 나지 않고 무시됨
        Mono.just(1)
            .ofType(String.class)
            .subscribe(System.out::println, (e) -> System.out.println("error"), () -> System.out.println("complete"));
    }

    public void doListener() {
        Mono.just("doOnSeries")
            .doOnSubscribe((consumer) -> System.out.println("doOnSubscribe"))
            .doOnRequest((consumer) -> System.out.println("doOnRequest"))
            .doOnNext((consumer) -> System.out.println("doOnNext"))
            .doOnEach((consumer) -> System.out.println("doOnEach"))
            .doOnCancel(() -> System.out.println("doOnCancel"))
            .doAfterTerminate(() -> System.out.println("doAfterTerminate"))
            .doOnTerminate(() -> System.out.println("doOnTerminate"))
            .doOnSuccess((consumer) -> System.out.println("doOnSuccess"))
            .doOnError((consumer) -> System.out.println("doOnError"))
            .doFinally((consumer) -> System.out.println("doFinally"))
            .doOnSubscribe((consumer) -> System.out.println("doOnSubscribe"))
            .subscribe();
    }
}
