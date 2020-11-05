package com.webflux.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.webflux.model.Test;

@RestController
public class RemoteController {
    @GetMapping("/remote/test1/{msg}")
    public Test greeting1(@PathVariable String msg) {
        return new Test("류승아", 32);
    }

    @GetMapping("/remote/test2/{msg}")
    public Test greeting2(@PathVariable String msg) {
        return new Test("이민지", 33);
    }
    
    @GetMapping("/remote/test3/{msg}")
    public Test greeting3(@PathVariable String msg) {
        return new Test("똥민지", 15);
    }
    
    @GetMapping("/remote/test4/{msg}")
    public Test greeting4(@PathVariable String msg) {
        return new Test("아제스", 22);
    }

    @GetMapping("/remote/test5/{msg}")
    public Test greeting5(@PathVariable String msg) {
        return new Test("김블럭", 17);
    }
}
