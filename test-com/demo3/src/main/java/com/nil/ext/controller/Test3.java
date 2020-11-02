package com.nil.ext.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test3 {

    @Value("${app.test.name:}")
    private String testName;

    @GetMapping("/test3")
    public String test3() {
        return "test3";
    }

    @GetMapping("/testName3")
    public String test1() {
        return "testName3--" + testName;
    }
}
