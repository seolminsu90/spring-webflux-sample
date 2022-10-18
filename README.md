# spring-webflux-sample

webflux sample 

- 기본 WebClient 사용 예
- 기본 r2dbc(ReactiveRedis) 사용 예
- 기본 reactive 모듈 사용 예
- Annotation 방식 및 RouterFunction 방식 두개 다 예시 있음.
- 기존 @Controller.. @PostMapping... 방식이 편하긴 한 듯
- MVC프로젝트에서 WebClient 사용 시 CountDownLatch 활용해야 한다.
  → 사용 쓰레드 수 정의하고 await하여 쓰레드 작업이 완료되는걸 기다리게 함.
  → 자세한건 찾아보기
----------------

## Reactor 쓸만해 보이는거 저장

### subscribeOn, publishOn
flatMap.. 등 사용 시 리턴하는 publisher를 subscribeOn으로 스레드풀로 스레드가 flatmap내부 처리를 병합처리해서 구독처리(subscribe) 할 수 있다 뭐가좋은지는 아직..
```bash
Flux.just("red", "white", "blue")
  .log()
  .flatMap(value -> 
      Mono.just(value.toUpperCase())
        .subscribeOn(Schedulers.parallel()),3)
        // 일부 예제 소스에 Schedulers.elastic()을 사용하는데(무제한 쓰레드풀) reactor에서는 deprecated 되었고 
        // boundedElastic()을(10 * number of CPU cores) 사용하라는 권고가 있으니 참조
  .subscribe(value -> {
      log.error("Consumed: " + value);
   });
```
-publishOn

next, complete, error신호를 별도 쓰레드로 처리할 수 있다. map(), flatMap() 등의 변환도 publishOn()이 지정한 쓰레드를 이용해서 처리한다
```bash
Flux.range(1, 6)
        .publishOn(Schedulers.newElastic("PUB1"), 2)
        .map(i -> {
            logger.info("map 1: {} + 10", i);
            return i + 10;
        })
        .publishOn(Schedulers.newElastic("PUB2"))
        .map(i -> {
            logger.info("map 2: {} + 10", i);
            return i + 10;
        })
        .subscribe();
```
-subscribeOn

 Subscriber가 시퀀스에 대한 request 신호를 별도 스케줄러로 처리한다. 즉 시퀀스(Flux나 Mono)를 실행할 스케줄러를 지정한다
```bash
Flux.range(1, 6)
        .log() 
        .subscribeOn(Schedulers.newElastic("SUB"))
        .map(i -> {
            logger.info("map: {} + 10", i);
            return i + 10;
        })
        .subscribe();

```
### log
reactor 동작이 로깅된다. (onSubscribe.. onNext.. complete) 의 과정
```bash
Flux.just(1, 2, 4, 5, 6)
     .log(null, Level.FINE) // java.util.logging.Level 타입
     .subscribe(x -> logger.info("next: {}", x));
```

### cold hot
대표적인 cold : Mono.create
대표적인 hot : Mono.just
hot : 구독하지 않아도 실행됨, 데이터 재사용 가능
cold : 구독해야 실행됨, 매번 새로운 데이터
```bash
public String test(){
  syso("hi");
  return "Hello"
}

Mono<String> hot = Mono.just(test());
hot.subscribe();
hot.subscribe();
hot.subscribe();

// hi
// hello
// hello
// hello
```
대부분 cold 이다, cold -> hot 전환
```bash
ConnectableFlux<Integer> hot = coldSource.publish();
```
여러번 가져다 쓸일 있을때 쓰면 좋을 듯?
