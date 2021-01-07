package com.webflux.router.function;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// functional 방식 Example
// ServerRequest에서 다양한 방식의 RequestParts를 제공한다..(form, body, queryparams, pathvariable 등)
// RequestPredicate 로 Path 및 Request의 조건 생성하고 route 연결 후 response 제공하는 방식 
// https://www.baeldung.com/spring-5-functional-web 에서 일반적인 요청도 참고하기
@Configuration
public class RouteFunctionExample {
    /**
     * dataBuffer 사용한 단독 업로드
     */
    @Bean
    public RouterFunction<ServerResponse> getEmployeeByIdRoute() {
        // 조건을 생성해서 사용 and... or...
        RequestPredicate predicate = RequestPredicates.POST(
                "/test2").and(RequestPredicates.accept(MediaType.APPLICATION_OCTET_STREAM));

        // 빌더 사용 예제
        // RouterFunctions.route().POST("/test2", RequestPredicates.accept(MediaType.APPLICATION_OCTET_STREAM), (req)->{return null;});
        
        RouterFunction<ServerResponse> response = RouterFunctions.route(predicate, (request) -> {
            Flux<DataBuffer> dataBuffer = request.body(BodyExtractors.toDataBuffers());
            Mono<String> mapper = uploadFilePart(dataBuffer).then(Mono.just("OK"));

            Mono<ServerResponse> res = ServerResponse.ok()
                                                     .contentType(MediaType.TEXT_PLAIN)
                                                     .body(BodyInserters.fromPublisher(mapper, String.class));
            return res;
        });

        return response;
    }

    /**
     * 일반적인 멀티파트
     */
    @Bean
    public RouterFunction<ServerResponse> getEmployeeByIdRouteTest() {
        RequestPredicate predicate = RequestPredicates.POST(
                "/test").and(RequestPredicates.accept(MediaType.APPLICATION_OCTET_STREAM));

        RouterFunction<ServerResponse> response = RouterFunctions.route(predicate, (request) -> {
            Mono<String> mapper = request.multipartData()
                                         .map(it -> it.get("files"))
                                         .flatMapMany(Flux::fromIterable)
                                         .cast(FilePart.class)
                                         .flatMap(it -> it.transferTo(Paths.get("/저장경로/" + it.filename())))
                                         .then(Mono.just("OK"));

            Mono<ServerResponse> res = ServerResponse.ok()
                                                     .contentType(MediaType.TEXT_PLAIN)
                                                     .body(BodyInserters.fromPublisher(mapper, String.class));
            return res;
        });

        return response;
    }

    public Flux<DataBuffer> uploadFilePart(Flux<DataBuffer> buffer) {
        try {
            String uuid = UUID.randomUUID().toString();
            Path tempFile = Files.createTempFile(Path.of("/저장경로/"), uuid, ".png");

            AsynchronousFileChannel channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE);
            return DataBufferUtils.write(buffer, channel, 0).doOnComplete(() -> {
                System.out.println("File Upload Success");
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Flux.error(e);
        }
    }
}
