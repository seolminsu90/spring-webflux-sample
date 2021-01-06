package com.webflux.rest;

import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.webflux.common.model.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ReactiveRestSample {
 // -------------------------------------------- 공통 Response객체 사용 시

    @GetMapping("/example/{userId}")
    public Mono<ResponseEntity<Test>> get(@PathVariable Long userId) {
        Mono<Test> testMono = Mono.just(new Test("test", 1));

        // map을 이용해서 Response를 변환
        return testMono.map((user) -> new ResponseEntity<Test>(user, HttpStatus.OK));
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
        return sampleOps.keys("*").flatMap(sampleOps.opsForValue()::get);
    }

    @PutMapping("/redis/test/{id}")
    public Mono<Boolean> insertSamples(@PathVariable String id) {
        return sampleOps.opsForValue().set(id, new Test("babo", 35));
    }

    // -------------------------------------------------- File 관련

    @PostMapping("/file")
    public Mono<String> uploadFile(@RequestPart("files") Flux<FilePart> filePartFlux) {

        // resume -> 대체후 구독, return -> 끊음
        return filePartFlux.flatMap(it -> it.transferTo(Paths.get("D:/tempDir/" + it.filename())))
                           .onErrorResume(e -> Mono.error(new RuntimeException("Exception..")))
                           .then(Mono.just("OK"));
    }

    @PostMapping("/file-model")
    public Mono<String> processModel(@ModelAttribute Model model) {
        model.getFiles().forEach(it -> it.transferTo(Paths.get("D:/tempDir/" + it.filename())));
        return Mono.just("OK");
    }

    public class Model {
        private List<FilePart> files;

        public List<FilePart> getFiles() {
            return files;
        }

        public void setFiles(List<FilePart> files) {
            this.files = files;
        }

    }
}
