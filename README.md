# spring-webflux-sample

webflux sample 

- 기본 WebClient 사용 예
- 기본 r2dbc(ReactiveRedis) 사용 예
- 기본 reactive 모듈 사용 예
- Annotation 방식 및 RouterFunction 방식 두개 다 예시 있음.
- 기존 @Controller.. @PostMapping... 방식이 편하긴 한 듯

----------------

쓸만해 보이는거 저장

### subscribeOn, publishOn
flatMap.. 등 사용 시 리턴하는 publisher를 subscribeOn으로 스레드풀로 스레드가 flatmap내부 처리를 병합처리해서 구독처리(subscribe) 할 수 있다 뭐가좋은지는 아직..
```bash
Flux.just("red", "white", "blue")
  .log()
  .flatMap(value ->
     Mono.just(value.toUpperCase())
       .subscribeOn(Schedulers.parallel()),3)
.subscribe(value -> {
    log.error("Consumed: " + value);
});
```
### log
reactor 동작이 로깅된다. (onSubscribe.. onNext.. complete) 의 과정
```bash
Flux.just(1, 2, 4, 5, 6)
     .log(null, Level.FINE) // java.util.logging.Level 타입
     .subscribe(x -> logger.info("next: {}", x));
```
