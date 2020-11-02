package com.nil.demo2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test2 {

    @GetMapping("/test2")
    public String test1() {
        return "test2";
    }
}
